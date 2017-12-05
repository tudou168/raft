package com.tongbanjie.raft.core.bootstrap;

import com.tongbanjie.raft.core.client.RaftClientBuilder;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.util.NetUtil;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * raft client main class
 * @author banxia
 * @date 2017-11-30 15:15:44
 */
public class RaftClientMainBootstrap {

    protected static final Map<String, String> commandMap = new HashMap<String, String>();

    private MyCommandOptions commandOptions = new MyCommandOptions();

    private RaftClientService raftClientService;
    private TransportClient transportClient;


    static {
        commandMap.put("connect", "host:port");
        commandMap.put("close", "");
        commandMap.put("raft:join", "host:port");
        commandMap.put("raft:leave", "host:port");
        commandMap.put("set", "key value");
        commandMap.put("get", "key");
        commandMap.put("quit", "");

    }

    public RaftClientMainBootstrap(String[] args) {
        commandOptions.parseOptions(args);
        System.out.println("Connecting to " + commandOptions.getOption("server"));
        connectToRaftClient(commandOptions.getOption("server"));
    }

    private void connectToRaftClient(String server) {

        String[] split = server.split(":");
        String host = split[0];
        Integer port = Integer.valueOf(split[1]);
        RaftClientBuilder<RaftClientService> builder = new RaftClientBuilder<RaftClientService>();
        this.raftClientService = builder.host(host).port(port).serviceInterface(RaftClientService.class).builder();
        this.transportClient = builder.getTransportClient();
        System.out.println("Connecting to " + server + " success!");


    }

    public static void main(String[] args) throws IOException {


        RaftClientMainBootstrap main = new RaftClientMainBootstrap(args);

        main.run();

    }

    private void run() throws IOException {

        if (commandOptions.getCommand() == null) {
            System.out.println("Welcome to Raft!");


            ConsoleReader reader = new ConsoleReader();

            String line = null;
            do {
                line = reader.readLine(String.format("%s-raft:[%s]>", NetUtil.getLocalAddress().getHostName(), commandOptions.getOption("server")));
                if (line != null) {
                    executeLine(line);
                }
            }
            while (line != null && !line.equals("quit"));

        } else {
            processCmd(commandOptions);
        }
    }


    private void executeLine(String line) {
        if (!line.equals("")) {
            commandOptions.parseCommand(line);
            processCmd(commandOptions);
        }

    }

    private boolean processCmd(MyCommandOptions commandOptions) {
        try {
            return processRaftCmd(commandOptions);
        } catch (IllegalArgumentException e) {
            System.err.println("Command failed: " + e);
        }
        return false;
    }

    private boolean processRaftCmd(MyCommandOptions commandOptions) {

        String[] args = commandOptions.getArgArray();
        String cmd = commandOptions.getCommand();
        if (args.length < 1) {
            usage();
            return false;
        }

        if (!commandMap.containsKey(cmd)) {
            usage();
            return false;
        }

        if (cmd.equals("quit")) {
            System.out.println("Quitting...");
            System.exit(1);
        } else if (cmd.equals("raft:join")) {
            System.out.println("Join...");
            System.exit(1);
        } else if (cmd.equals("raft:leave")) {
            System.out.println("Leave...");
            System.exit(1);
        } else if (cmd.equals("close")) {
            System.out.println("Closing the raft client...");
            System.exit(1);
        } else if (cmd.equals("set")) {
            System.out.println("Set the raft value ...");
            System.exit(1);
        } else if (cmd.equals("get")) {
            System.out.println("Get the raft  value...");
            System.exit(1);
        }

        return true;
    }


    private void usage() {
        System.err.println("Raft -server host:port cmd args");
        for (String cmd : commandMap.keySet()) {
            System.err.println("\t" + cmd + " " + commandMap.get(cmd));
        }
    }


    private static class MyCommandOptions {


        private Map<String, String> options = new HashMap<String, String>();
        private List<String> cmdArgs = null;
        private String command = null;
        public static final Pattern ARGS_PATTERN = Pattern.compile("\\s*([^\"\']\\S*|\"[^\"]*\"|'[^']*')\\s*");
        public static final Pattern QUOTED_PATTERN = Pattern.compile("^([\'\"])(.*)(\\1)$");


        public MyCommandOptions() {
            options.put("server", "localhost:7001");
            options.put("timeout", "30000");
        }


        public String getOption(String opt) {

            return this.options.get(opt);
        }

        public String getCommand() {
            return command;
        }

        public String getCmdArgument(int index) {
            return this.cmdArgs.get(index);
        }

        public int getNumArguments() {
            return this.cmdArgs.size();
        }

        public String[] getArgArray() {
            return cmdArgs.toArray(new String[0]);
        }


        public boolean parseOptions(String[] args) {

            List<String> argList = Arrays.asList(args);

            Iterator<String> it = argList.iterator();

            while (it.hasNext()) {

                String opt = it.next();

                try {

                    if (opt.equals("-server")) {
                        options.put("server", it.next());
                    } else if (opt.equals("-timeout")) {
                        options.put("timeout", it.next());
                    }
                } catch (Exception e) {

                    System.err.println("Error: no argument found for option:" + opt);
                }


                if (!opt.startsWith("-")) {
                    command = opt;
                    cmdArgs = new ArrayList<String>();
                    while (it.hasNext()) {
                        cmdArgs.add(it.next());
                    }
                    return true;
                }

            }

            return true;
        }

        /**
         * Breaks a string into command + arguments.
         *
         * @param cmdstring string of form "cmd arg1 arg2..etc"
         * @return true if parsing succeeded.
         */
        public boolean parseCommand(String cmdstring) {
            Matcher matcher = ARGS_PATTERN.matcher(cmdstring);

            List<String> args = new LinkedList<String>();
            while (matcher.find()) {
                String value = matcher.group(1);
                if (QUOTED_PATTERN.matcher(value).matches()) {
                    // Strip off the surrounding quotes
                    value = value.substring(1, value.length() - 1);
                }
                args.add(value);
            }
            if (args.isEmpty()) {
                return false;
            }
            command = args.get(0);
            cmdArgs = args;
            return true;
        }
    }

}
