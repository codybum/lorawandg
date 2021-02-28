package io.cresco.lorawandg;

import com.google.gson.Gson;
import io.cresco.library.data.TopicType;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

import javax.jms.TextMessage;
import java.util.Random;

public class MessageSender implements Runnable  {

    private PluginBuilder plugin;
    private CLogger logger;
    private Gson gson;
    //private MeasurementEngine me;
    String inputStreamName;


    //public MessageSender(PluginBuilder plugin, MeasurementEngine me) {
    public MessageSender(PluginBuilder plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger(this.getClass().getName(), CLogger.Level.Info);
        gson = new Gson();
        inputStreamName = plugin.getConfig().getStringParam("input_stream_name",plugin.getPluginID().replace("-",""));
        //this.me = me;
        //set metrics
        //metricInit();

    }

    private void metricInit() {

        //me.setTimer("message.send.time", "The timer for cep messages", "cep_send");
        //me.setGauge("message.send.value", "The timer for cep messages", "cep_send", CMetric.MeasureClass.GAUGE_DOUBLE);
        //me.setGauge("cep.transaction.time.g.i", "The timer for cep messages", "cep", CMetric.MeasureClass.GAUGE_INT);
        //me.setGauge("cep.transaction.time.g.l", "The timer for cep messages", "cep", CMetric.MeasureClass.GAUGE_LONG);
        //me.setGauge("cep.transaction.time.g.d", "The timer for cep messages", "cep", CMetric.MeasureClass.GAUGE_DOUBLE);
        //me.setDistributionSummary("cep.transaction.time.ds", "The timer for cep messages", "cep");

    }

    public void run() {

        try {
            while (!plugin.isActive()) {
                logger.info("Sender: Wait until plugin is active...");
                Thread.sleep(1000);
            }
            logger.info("Sender: Sending to input_stream_name: " + inputStreamName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while(plugin.isActive()) {
            try {
                long t0 = System.currentTimeMillis();
                //send a message once a second
                sendIt();
                Thread.sleep(1000);
                //long diff = System.currentTimeMillis() - t0;
                //me.updateTimer("message.send.time", diff);

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendIt() {

        try {

            //String inputStreamName = "UserStream";

            //String inputStreamName = plugin.getConfig().getStringParam("input_stream_name",plugin.getPluginID().replace("-",""));
            //String inputStreamName = plugin.getConfig().getStringParam("pluginID").replace("-","");

            TextMessage tickle = plugin.getAgentService().getDataPlaneService().createTextMessage();
            tickle.setText(getStringPayload());
            tickle.setStringProperty("stream_name",inputStreamName);

            plugin.getAgentService().getDataPlaneService().sendMessage(TopicType.AGENT,tickle);
            /*
            me.updateTimer("cep.transaction.time", diff);
            me.updateIntGauge("cep.transaction.time.g.i", 123);
            me.updateLongGauge("cep.transaction.time.g.l", 1234567890123456788l);
            me.updateDoubleGauge("cep.transaction.time.g.d", 12345.6789);
            me.updateDistributionSummary("cep.transaction.time.ds",t0);
             */

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getStringPayload() {

        String rec = null;

        try{

            String source = plugin.getConfig().getStringParam("source_name","mysource");
            String urn = plugin.getConfig().getStringParam("urn","myurn");
            String metric = plugin.getConfig().getStringParam("metric_name","mymetric");
            long ts = System.nanoTime();

            Random r = new Random();
            double value = r.nextDouble() * 100;

            Ticker tick = new Ticker(source, urn, metric, ts, value);

            rec = gson.toJson(tick);

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return rec;
    }



}
