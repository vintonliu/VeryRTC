package club.apprtc.veryrtc;

public interface OnClientListener {
    /**
     * Callback fired once user login/logout to/from proxy server
     * @param isLogin true for login successful and false for logout successful
     */
    void onLoginSuccessed(final boolean isLogin);

    /**
     * Callback fired once user login or keep alive failure
     * @param description detail description
     */
    void onLoginFailure(final String description);

    /**
     * Callback fired once an outgoing call received remote peer ringing message
     */
    void onCallRinging();

    /**
     * Callback fired once an incoming call received
     * @param fromUser caller's username
     * @param videoCall if video incoming call
     */
    void onCallIncoming(final String fromUser, final boolean videoCall);

    /**
     * Callback fired once the call established
     * @param videoCall if connected as video call
     */
    void onCallConnected(boolean videoCall);

    /**
     * Callback fired once the call terminate
     * @param description detail description
     */
    void onCallEnded(final String description);

    /**
     * Callback fired once call statics report ready
     * @param encoderStat
     * @param bweStat
     * @param connectionStat
     * @param videoSendStat
     * @param videoRecvStat
     */
    void onCallStatsReady(final String encoderStat,
                          final String bweStat,
                          final String connectionStat,
                          final String videoSendStat,
                          final String videoRecvStat);

    /**
     * Callback fired once an internal error occoured
     * @param description error detail description
     */
    void onClientError(final String description);
}
