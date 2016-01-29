package xyz.nickr.superchat.cmd.util;

import xyz.nickr.superchat.cmd.Command;
import xyz.nickr.superchat.sys.Group;
import xyz.nickr.superchat.sys.Message;
import xyz.nickr.superchat.sys.Sys;
import xyz.nickr.superchat.sys.User;

public class JenkinsCommand implements Command {

    @Override
    public String[] names() {
        return new String[] { "jenkins" };
    }

    @Override
    public String[] help(User user, boolean userChat) {
        return new String[] { "", "tells you the bot's jenkins" };
    }

    @Override
    public boolean userchat() {
        return true;
    }

    @Override
    public boolean alwaysEnabled() {
        return true;
    }

    @Override
    public void exec(Sys sys, User user, Group conv, String used, String[] args, Message message) {
        conv.sendMessage(sys.message().link("http://ci.nickr.xyz/view/SuperChat/").text("http://ci.nickr.xyz/view/SuperChat/").build());
    }

}