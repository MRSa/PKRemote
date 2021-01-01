package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command;

public interface IPtpIpCommand
{
    // メッセージの識別子
    int getId();

    // 短い長さのメッセージを受け取ったときに再度受信するか
    boolean receiveAgainShortLengthMessage();

    // シーケンス番号を埋め込むかどうか
    boolean useSequenceNumber();

    // シーケンス番号を更新（＋１）するかどうか
    boolean isIncrementSeqNumber();

    // コマンドの受信待ち時間(単位:ms)
    int receiveDelayMs();

    // 埋め込むシーケンス番号の位置
    int embeddedSequenceNumberIndex();

    // 埋め込むシーケンス番号の位置
    int embeddedSequenceNumberIndex2();

    // 埋め込むシーケンス番号の位置
    int embeddedSequenceNumberIndex3();

    // 予定している受信データのサイズ
    int estimatedReceiveDataSize();

    // 送信するメッセージボディ
    byte[] commandBody();

    // 送信するメッセージボディ(連続送信する場合)
    byte[] commandBody2();

    // 送信するメッセージボディ(連続送信する場合)
    byte[] commandBody3();

    // コマンド送信結果（応答）の通知先
    IPtpIpCommandCallback responseCallback();

    //  特定シーケンスを特定するID
    int getHoldId();

    // 特定シーケンスに入るか？
    boolean isHold();

    // 特定シーケンスから出るか？
    boolean isRelease();

    // デバッグ用： ログ(logcat)に通信結果を残すかどうか
    boolean dumpLog();

    // リトライオーバー発生時、コマンドを再送するか？
    boolean isRetrySend();

    // 最後に1回余計に受信をするか？
    boolean isLastReceiveRetry();

    // 受信待ち再試行回数
    int maxRetryCount();

    // リトライオーバーで再送するとき、SeqNoをインクリメントするか
    boolean isIncrementSequenceNumberToRetry();

}
