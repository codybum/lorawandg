package lorawandg;


import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Component(
        service = { PluginService.class },
        scope=ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        reference=@Reference(name="io.cresco.library.agent.AgentService", service=AgentService.class)
)

public class Plugin implements PluginService {

    public BundleContext context;
    public PluginBuilder pluginBuilder;
    private Executor executor;
    private CLogger logger;
    private Map<String, Object> map;
    public String myname;
    private Thread messageSenderThread;

    @Override
    public boolean isActive() {
        return pluginBuilder.isActive();
    }

    @Override
    public void setIsActive(boolean isActive) {
        pluginBuilder.setIsActive(isActive);
    }

    @Activate
    void activate(BundleContext context, Map<String, Object> map) {

        this.context = context;
        this.map = map;
        myname = "this is my name2";

    }

    @Modified
    void modified(BundleContext context, Map<String, Object> map) {
        System.out.println("Modified Config Map PluginID:" + (String) map.get("pluginID"));
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        pluginBuilder.msgIn(incoming);
        return true;
    }

    @Deactivate
    void deactivate(BundleContext context, Map<String,Object> map) {

        isStopped();
        this.context = null;
        this.map = null;

    }

    @Override
    public boolean isStarted() {

        try {
            logger.error("WOOR");
            pluginBuilder = new PluginBuilder(this.getClass().getName(), context, map);
            this.logger = pluginBuilder.getLogger(Plugin.class.getName(), CLogger.Level.Info);
            //this.executor = new ExecutorImpl(pluginBuilder, me);
            this.executor = new ExecutorImpl(pluginBuilder);
            pluginBuilder.setExecutor(executor);

            while (!pluginBuilder.getAgentService().getAgentState().isActive()) {
                logger.info("Plugin " + pluginBuilder.getPluginID() + " waiting on Agent Init");
                Thread.sleep(1000);
            }
            //me = new MeasurementEngine(pluginBuilder);

            //configMode = pluginBuilder.getConfig().getIntegerParam("mode",-1);
            logger.info("Starting LoRaWAN DataGenerator " + pluginBuilder.getPluginID());
            //send a bunch of messages
            //MessageSender messageSender = new MessageSender(pluginBuilder,me);
            MessageSender messageSender = new MessageSender(pluginBuilder);
            messageSenderThread = new Thread(messageSender);
            messageSenderThread.start();


            logger.info("Started PluginID:" + (String) map.get("pluginID"));

            //set plugin active
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        pluginBuilder.setExecutor(null);
        pluginBuilder.setIsActive(false);
        try {
                if (messageSenderThread.isAlive()) {
                    messageSenderThread.join();
                }

        } catch (Exception ex) {
            logger.error("Waiting on thread: " + ex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.error(sw.toString());
        }
        return true;
    }
}