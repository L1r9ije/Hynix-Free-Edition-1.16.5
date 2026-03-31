package mods.voicechat.debug;

import mods.voicechat.Voicechat;

public class VoicechatUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Voicechat.LOGGER.error("Uncaught exception in thread {}", t.getName(), e);
    }

}
