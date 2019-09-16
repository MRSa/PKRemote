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

    // 送信するメッセージボディ
    byte[] commandBody();

    // 送信するメッセージボディ(連続送信する場合)
    byte[] commandBody2();

    // コマンド送信結果（応答）の通知先
    IPtpIpCommandCallback responseCallback();

    // デバッグ用： ログ(logcat)に通信結果を残すかどうか
    boolean dumpLog();
}
