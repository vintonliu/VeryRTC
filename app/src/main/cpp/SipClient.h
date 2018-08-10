/**
 * SipClient.h
 */
#ifndef __SIP_CLIENT_H__
#define __SIP_CLIENT_H__
#include <memory>
#include <string>
#include <thread>
#include <vector>
#include <mutex>
#include "coreapi/linphonecore.h"
#include "SignalingEvents.h"

#ifdef _WIN32
#ifdef DLL_API_EXPORTS
#define MDLL_API __declspec(dllexport)
#pragma warning(disable:4251)
#else  
#define MDLL_API __declspec(dllimport)  
#endif
#else
#define MDLL_API
#endif

namespace mrtc {
typedef enum {
	MSipTransportUDP, /*UDP*/
	MSipTransportTCP, /*TCP*/
	MSipTransportTLS, /*TLS*/
	MSipTransportDTLS /*DTLS*/
} MSipTransport;

class MDLL_API SipClient
{
public:
	explicit SipClient(SignalingEvents *events);
	~SipClient();

	/**
	 * Initialize client
	 */
	int32_t initialize();

	/**
	 * Initialize Linphone core callback table
	 */
	void initVtable();

	/**
	 * Set sip listen transport and local port
	 * @param transport see @MSipTransport
	 * @port  local listen port
	 */
	int32_t doSetSipTransport(MSipTransport transport, uint32_t port);

	/**
	 * Build and send Initialize REGISTER message.
	 * @param proxy			register proxy
	 * @param display		sip url display name
	 * @param username	sip url username
	 * @param authname	Register authentication name
	 * @param authpwd		Register authentication password
	 * @param realm			Register authentication realm
	 */
	int32_t doRegister(const std::string &proxy, const std::string &display,
						const std::string &username, const std::string &authname,
						const std::string &authpwd, const std::string &realm = "");

	/**
	 * Send REGISTER Message with expire = 0
	 */
	int32_t doUnRegister();

	/**
	 * Build and send Initialize INVITE message
	 * @param callee The peer number or identity would fill to INVITE To Header
	 * @param offer Callers' SDP
	 */
	int32_t doStartCall(const std::string &callee, const std::string &offer = "");

	/**
	 * Build and send 200OK to accept the call
	 * @param answer Callees' SDP
	 */
	int32_t doAcceptCall(const std::string &answer = "");

	/**
	 * Build and send BYE to terminate the connected call,
	 * or Send CANCEL to abort the outgoing call,
	 * or Send 603 to reject the incoming call
	 */
	int32_t doHangup();

	/**
	 * Build and send MESSAGE with candidate body formated in Json
	 */
	int32_t doSendCandidate(const std::string &candidate = "");
	
	bool doSetUserAgent(const std::string &uname, const std::string &uver);

	/* LinphoneCoreVTable callbacks */
	static void globalStateCb(LinphoneCore *lc, LinphoneGlobalState gstate, const char *message);
	static void callStateCb(LinphoneCore *lc, LinphoneCall *call, LinphoneCallState cstate, const char *message);
	static void registrationStateCb(LinphoneCore *lc, LinphoneProxyConfig *cfg, LinphoneRegistrationState cstate, const char *message);
	static void displayStatusCb(LinphoneCore *lc, const char *message);
	static void callLogUpdated(LinphoneCore *lc, LinphoneCallLog *newcl);
	static void displayMessage(LinphoneCore *lc, const char *message);
	static void displayWarning(LinphoneCore *lc, const char *message);
	static void textReceivedCb(LinphoneCore *lc, const LinphoneAddress *from, const char *message);
	
private:
	

protected:
	void SipIterate();

private:
	LinphoneCore * _ptrLc;
	LinphoneCoreVTable _vtable;
	SignalingEvents *_events;

	bool _running{ false };
	std::unique_ptr<std::thread> _iterate_thread;
	const char* _thread_name;	
};
} /* namespace mrtc */
#endif /* __SIP_CLIENT_H__ */