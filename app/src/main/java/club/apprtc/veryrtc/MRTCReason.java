package club.apprtc.veryrtc;

public enum MRTCReason {
    /* No explicit error */
    MRTC_REASON_NONE("成功"),

    /* Request sent but not received any responses */
    MRTC_REASON_NO_RESPONSE("请求无响应"),
    /* Credentials failure, bad username or password */
    MRTC_REASON_BAD_CREDENTIALS("认证失败"),
    /* Maybe username un-exist or offline */
    MRTC_REASON_NOT_FOUND("用户不存在或离线"),
    /* Server internal error received */
    MRTC_REASON_SERVER_INTERNAL_ERROR("服务器内部错误"),
    /* Request sent but not received finally response */
    MRTC_REASON_REQUEST_TIMEOUT("请求超时"),
    /* Request sent to peer, but peer have unavailable flag */
    MRTC_REASON_TEMPORARILY_UNAVAILABLE("对端异常"),

    /* Caller hangup the call */
    MRTC_REASON_USER_HANGUP("本端挂断通话"),
    /* Caller cancel the call */
    MRTC_REASON_USER_CANCEL("呼叫取消"),
    /* Callee hangup the call */
    MRTC_REASON_PEER_HANGUP("对方已挂断"),
    /* Callee reject to answer the call */
    MRTC_REASON_PEER_DECLINED("对方拒接"),
    /* Callee not answer the call until timeout(60s) */
    MRTC_REASON_PEER_NO_ANSWER("对方未接听"),
    /* Callee busy now */
    MRTC_REASON_PEER_BUSY("对方正在通话中"),

    /* Maybe SDP create or set failed */
    MRTC_REASON_SDP_FAILURE("SDP 错误"),
    /* Call failure since ice connect failed */
    MRTC_REASON_ICE_FAILURE("ICE 错误"),
    /* Caller and Callee media capabilities negotiation failure */
    MRTC_REASON_MEDIA_NOT_ACCEPT("媒体协商失败"),

    MRTC_REASON_UNKNOWN("未知错误")
    ;

    private String desc;
    private MRTCReason(String desc) {
        this.desc = desc;
    }


    @Override
    public String toString() {
        return desc;
    }
}
