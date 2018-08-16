#include "SipClient.h"
#include <iostream>
#include <thread>
#include <chrono>

using namespace std;
namespace mrtc {

#define INITIALIZED_CHECK_VALUE(value) \
	if (linphone_core_get_global_state(_ptrLc) != LinphoneGlobalOn) \
	{ \
		return value;\
	}

#define INITIALIZED_CHECK() \
	if (linphone_core_get_global_state(_ptrLc) != LinphoneGlobalOn) \
	{ \
		return;\
	}
	
SipClient::SipClient(SignalingEvents * events)
	: _ptrLc(nullptr)
	, _events(events)
	, _thread_name("LcIterateThread")
	, _running(false)
{
	initialize();
}
	
SipClient::~SipClient()
{
	if (_running)
	{
		_running = false;
		_iterate_thread->join();
		_iterate_thread.reset();
	}
	
	if (_ptrLc)
	{
		linphone_core_destroy(_ptrLc);
		_ptrLc = nullptr;
	}	
}

int32_t SipClient::initialize()
{
	initVtable();

	OrtpLogLevel lvl = (OrtpLogLevel)(ORTP_MESSAGE | ORTP_WARNING | ORTP_ERROR | ORTP_FATAL);
	linphone_core_set_log_level(lvl);
	_ptrLc = linphone_core_new_with_config(&_vtable, this);

	// random local port
	doSetSipTransport(MSipTransportTLS, 0);
	
	_running = true;
	_iterate_thread.reset(new std::thread(&SipClient::SipIterate, this));
	
	return 0;
}

void SipClient::initVtable()
{
	_vtable.global_state_changed = globalStateCb;
	_vtable.registration_state_changed = registrationStateCb;
	_vtable.call_state_changed = callStateCb;
	_vtable.auth_info_requested = nullptr;
	_vtable.call_log_updated = callLogUpdated;
	_vtable.dtmf_received = nullptr;
	_vtable.refer_received = nullptr;
	_vtable.text_received = textReceivedCb;
	_vtable.call_encryption_changed = nullptr;
	_vtable.transfer_state_changed = nullptr;
	_vtable.notify_recv = nullptr;
	_vtable.display_status = displayStatusCb;
	_vtable.display_message = displayMessage;
	_vtable.display_warning = displayWarning;
	_vtable.display_url = nullptr;
	_vtable.show = nullptr;
}

int32_t SipClient::doSetSipTransport(MSipTransport transport, uint32_t port)
{
	INITIALIZED_CHECK_VALUE(-1);
	LCSipTransports tr;
	tr.transport = (LCSipTransport)transport;
	tr.udp_port = tr.dtls_port = tr.tcp_port = tr.tls_port = port;

	return linphone_core_set_sip_transports(_ptrLc, &tr);
}

int32_t SipClient::doRegister(const std::string &proxy, const std::string &display,
							const std::string &username, const std::string &authname,
							const std::string &authpwd, const std::string &realm)
{
	INITIALIZED_CHECK_VALUE(-1);

	char identity[256] = { 0 };
	uint32_t port = 5060;
	LCSipTransports lc;
	std::string tmpProxy = proxy;

	if (proxy.empty() || username.empty() || authname.empty()	|| authpwd.empty())
	{
		return -1;
	}

	memset(&lc, 0x00, sizeof(LCSipTransports));
	linphone_core_get_sip_transports(_ptrLc, &lc);

	std::size_t found = proxy.find(':');
	if (found == std::string::npos)
	{
		if (lc.transport == LcTransportDTLS || lc.transport == LcTransportTLS)
		{
			tmpProxy += ":5061";
		}
	}

	if (!display.empty())
	{
		snprintf(identity, sizeof(identity), "\"%s\"<sip:%s@%s>", 
					display.c_str(), username.c_str(), tmpProxy.c_str());
	}
	else
	{
		snprintf(identity, sizeof(identity), "sip:%s@%s", username.c_str(), tmpProxy.c_str());
	}

	// Register for new user
	LinphoneProxyConfig *proxy_cfg = nullptr;
	LinphoneAuthInfo *auth_info = nullptr;

	auth_info = linphone_auth_info_new(authname.c_str(), username.c_str(), authpwd.c_str(), NULL, realm.c_str());
	linphone_core_add_auth_info(_ptrLc, auth_info);
	linphone_auth_info_destroy(auth_info);

	proxy_cfg = linphone_proxy_config_new();
	linphone_proxy_config_set_identity(proxy_cfg, identity);
	linphone_proxy_config_set_server_addr(proxy_cfg, tmpProxy.c_str());
	linphone_proxy_config_set_expires(proxy_cfg, 1800);
	linphone_proxy_config_enable_register(proxy_cfg, TRUE);
	linphone_core_add_proxy_config(_ptrLc, proxy_cfg);
	linphone_core_set_default_proxy(_ptrLc, proxy_cfg);


	return 0;
}

int32_t SipClient::doUnRegister()
{
	INITIALIZED_CHECK_VALUE(-1);

	LinphoneProxyConfig *proxy_cfg = nullptr;

	linphone_core_get_default_proxy(_ptrLc, &proxy_cfg); /* get default proxy config*/
	linphone_proxy_config_edit(proxy_cfg); /*start editing proxy configuration*/
	linphone_proxy_config_enable_register(proxy_cfg, FALSE); /*de-activate registration for this proxy config*/
  return linphone_proxy_config_done(proxy_cfg); /*initiate REGISTER with expire = 0*/
}

int32_t SipClient::doStartCall(const std::string &callee, const std::string &offer)
{
	INITIALIZED_CHECK_VALUE(-1);

	if (callee.empty() || offer.empty())
	{
		return -1;
	}

	if (linphone_core_invite_sdp(_ptrLc, callee.c_str(), offer.c_str()) == nullptr)
	{
		return -1;
	}

	return 0;
}

int32_t SipClient::doAcceptCall(const std::string &answer)
{
	INITIALIZED_CHECK_VALUE(-1);
	if (answer.empty())
	{
		return -1;
	}

	linphone_call_set_local_sdp_str(linphone_core_get_current_call(_ptrLc), answer.c_str());
	return linphone_core_accept_call(_ptrLc, linphone_core_get_current_call(_ptrLc));
}

int32_t SipClient::doHangup()
{
	INITIALIZED_CHECK_VALUE(-1);

	return linphone_core_terminate_all_calls(_ptrLc);
}

int32_t SipClient::doSendCandidate(const std::string & candidate)
{
	INITIALIZED_CHECK_VALUE(-1);
	if (candidate.empty())
	{
		return -1;
	}

	return linphone_call_send_candidate_message(linphone_core_get_current_call(_ptrLc), candidate.c_str());
}

bool SipClient::doSetUserAgent(const std::string & uname, const std::string & uver)
{
	INITIALIZED_CHECK_VALUE(false);

	if (uname.empty() || uver.empty())
	{
		return false;
	}

	linphone_core_set_user_agent(_ptrLc, uname.c_str(), uver.c_str());
	return true;
}

void SipClient::globalStateCb(LinphoneCore * lc, LinphoneGlobalState gstate, const char * message)
{
	if (message != nullptr)
	{
		ms_debug("globalState: %s", message);
	}
}

void SipClient::callStateCb(LinphoneCore * lc, LinphoneCall * call, LinphoneCallState cstate, const char * message)
{
	//char *from = linphone_call_get_remote_address_as_string(call);
	const LinphoneAddress *fromaddr = linphone_call_get_remote_address(call);
	const char *from = linphone_address_get_username(fromaddr);

	SipClient *sipclient = static_cast<SipClient*>(linphone_core_get_user_data(lc));
	
	ms_message("Call from %s state %s", from, linphone_call_state_to_string(cstate));

	switch (cstate) {
	case LinphoneCallEnd:
	case LinphoneCallError:
	{
		ms_message("Call ended (%s).", linphone_reason_to_string(linphone_call_get_reason(call)));
		
		if (sipclient->_events)
		{
			sipclient->_events->onCallEnded((SipReason)(linphone_call_get_reason(call)));
		}
	}
		break;
	case LinphoneCallResuming:
	{
		/// ToDo
	}
		break;
	case LinphoneCallStreamsRunning:
	{
		if (sipclient->_events)
		{
			//sipclient->_events->onCallConnected();
		}
	}
		break;
	case LinphoneCallPausing:
	{
		/// ToDo
	}
		break;
	case LinphoneCallPaused:
	{
		/// ToDo
	}
		break;
	case LinphoneCallPausedByRemote:
	{
		/// ToDo
	}
		break;
	case LinphoneCallIncomingReceived:
	{
		if (sipclient->_events)
		{
			SignalingParameters param;
			param.from = from;
			param.rsdp = linphone_call_get_remote_sdp_str(call);
			sipclient->_events->onCallIncoming(param);
		}
	}
		break;
	case LinphoneCallOutgoingInit:
	{
		/// ToDo
	}
		break;
	case LinphoneCallUpdatedByRemote:
		linphone_core_defer_call_update(lc, call);
		break;
	case LinphoneCallOutgoingProgress:
	{
		if (sipclient->_events)
		{
			sipclient->_events->onCallProcess();
		}
	}
		break;
	case LinphoneCallOutgoingRinging:
	{
		if (sipclient->_events)
		{
			sipclient->_events->onCallRinging();
		}
	}
		break;
	case LinphoneCallConnected:
	{
		if (sipclient->_events)
		{
			SignalingParameters param;
			param.from = from;
			param.rsdp = linphone_call_get_remote_sdp_str(call);
			sipclient->_events->onCallConnected(param);
		}
	}
		break;
	case LinphoneCallOutgoingEarlyMedia:
	{
		if (sipclient->_events)
		{
			sipclient->_events->onCallRinging();
		}
	}
		break;
	
	default:
		break;
	}

	//ms_free(from);
}

void SipClient::registrationStateCb(LinphoneCore * lc, LinphoneProxyConfig * cfg, LinphoneRegistrationState cstate, const char * message)
{
	ms_message("New registration state %s for user id [%s] at proxy [%s]."
				, linphone_registration_state_to_string(cstate)
				, linphone_proxy_config_get_identity(cfg)
				, linphone_proxy_config_get_addr(cfg));

	SipClient *client = static_cast<SipClient*>(linphone_core_get_user_data(lc));
	switch (cstate)
	{
	case LinphoneRegistrationOk:
	{
		if (client->_events)
		{
			client->_events->onRegistered(true);
		}
	}
	break;

	case LinphoneRegistrationCleared:
	{
		if (client->_events)
		{
			client->_events->onRegistered(false);			
		}
		linphone_core_clear_proxy_config(lc);
	}
	break;

	case LinphoneRegistrationFailed:
	{
		if (client->_events)
		{
			LinphoneReason reason = linphone_proxy_config_get_error(cfg);
			
			if (reason == LinphoneReasonBadCredentials || reason == LinphoneReasonNotFound)
			{
				linphone_core_clear_proxy_config(lc);
			}
			client->_events->onRegisterFailure((SipReason)reason);
		}
	}
		break;

	default:
		break;
	}
}

void SipClient::displayStatusCb(LinphoneCore * lc, const char * message)
{
	if (message != nullptr)
	{
		ms_debug("display status: %s.", message);
	}
}

void SipClient::callLogUpdated(LinphoneCore * lc, LinphoneCallLog * newcl)
{
	if (newcl != nullptr)
	{
		char * cl = linphone_call_log_to_str(newcl);
		ms_message("Call log: %s", cl);
		ms_free(cl);
		cl = nullptr;
	}
}

void SipClient::displayMessage(LinphoneCore * lc, const char * message)
{
	if (message != nullptr)
	{
		ms_debug("display message: %s.", message);
	}
}

void SipClient::displayWarning(LinphoneCore * lc, const char * message)
{
	if (message != nullptr)
	{
		ms_warning("display warning: %s.", message);
	}
}

void SipClient::textReceivedCb(LinphoneCore * lc, const LinphoneAddress * from, const char * message)
{
	SipClient *client = static_cast<SipClient*>(linphone_core_get_user_data(lc));
	if (client->_events)
	{
		client->_events->onRemoteIceCandidate(message);
	}
}

void SipClient::SipIterate()
{
	while (_running)
	{
		linphone_core_iterate(_ptrLc);

		std::this_thread::sleep_for(std::chrono::milliseconds(50));
	}	
}
} /* namespace msip */