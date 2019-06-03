package com.orbis.advisory;

import java.io.*;
import java.util.*;
import com.github.*;
import com.github.sd.*;
import org.json.*;
import static com.orbis.advisory.AdvisoryEndpoints.*;

/**
 * User: Daniil Sosonkin
 * Date: 5/29/2019 12:46 PM
 */
public class ModelOrderCancelTest
    {
        public static void main(String[] args) throws Exception
            {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream("advisory.conf"))
                    {
                        props.load(in);
                    }

                String domain = props.getProperty("domain");
                String platformId = props.getProperty("platform.id");
                String username = props.getProperty("username");
                String password = props.getProperty("password");

                OrbisAPI api = new OrbisAPI();
                api.setCredentials(new AvisoryCredentials(domain, platformId, username, password));
                api.setHostname(domain);

                EquityOrder order = new EquityOrder();
                order.setQuote(new Quote("IBM"));
                order.setOrderType(OrderType.LIMIT);
                order.setQuantity(2_000);
                order.setLimitPrice(1);
                order.setTransaction(Transaction.BUY);

                AdvisoryEquityOrder request = new AdvisoryEquityOrder();
                request.setModel(new PortfolioModel().setId(1));
                request.setOrder(order);

                JSONObject resp = api.post(AdvisoryModelPlaceEquity, new JSONObject(request));
                order.setOrderRef(resp.getString("OrderRef"));
                System.out.println("Order placed as: " + order.getOrderRef());

                waitForAccept(api, order.getOrderRef());

                JSONObject jo = new JSONObject();
                jo.put("orderRef", order.getOrderRef());

                JSONObject cancel = new JSONObject();
                cancel.put("order", jo);
                System.out.println(api.post(OrderCancel, cancel).toString());

            }

        private static void waitForAccept(OrbisAPI api, String orderRef) throws IOException, InterruptedException
            {
                String status;
                do
                    {
                        Thread.sleep(1000);
                        JSONObject result = api.get(OrdersStatus, "{orderRef}", orderRef);
                        status = result.getString("translatedStatus");

                    } while (!"W".equals(status));
            }
    }