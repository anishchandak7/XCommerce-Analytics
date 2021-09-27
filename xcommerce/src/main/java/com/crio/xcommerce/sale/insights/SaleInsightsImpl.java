package com.crio.xcommerce.sale.insights;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.crio.xcommerce.contract.exceptions.AnalyticsException;
import com.crio.xcommerce.contract.insights.SaleAggregate;
import com.crio.xcommerce.contract.insights.SaleAggregateByMonth;
import com.crio.xcommerce.contract.insights.SaleInsights;
import com.crio.xcommerce.contract.resolver.DataProvider;

public class SaleInsightsImpl implements SaleInsights {

    private BufferedReader bReader;

    @Override
    public SaleAggregate getSaleInsights(DataProvider dataProvider, int year)
            throws IOException, AnalyticsException {
        // TODO Auto-generated method stub
        String provider = dataProvider.getProvider();
        File csvFile = dataProvider.resolveFile();
        double totalSales = 0.0;
        double[][] monthWiseSales = new double[13][1];
        LocalDate date;
        double amount;
        List<SaleAggregateByMonth> aggregateByMonths = null;
        SaleAggregate saleAggregate = new SaleAggregate();
        bReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));

        try {
            String s = bReader.readLine(); // Skipping the first Row
        } catch (final Exception e) {
            throw new AnalyticsException("File Not Read Properly!");
        }
        String line = "";
        String DELIMETER = ",";

        if (provider.equalsIgnoreCase("flipkart") == true) {
            // transaction_id,external_transaction_id,user_id,transaction_date,transaction_status,amount
            // 0 transaction_id,
            // 1 external_transaction_id,
            // 2 user_id,
            // 3 transaction_date,
            // 4 transaction_status,
            // 5 amount
            List<String> validStatus = Arrays.asList("complete", "paid", "shipped");
            //final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while ((line = bReader.readLine()) != null) {
                String temp[] = line.split(DELIMETER);
                try {
                    date = LocalDate.parse(temp[3]);
                    amount = Double.parseDouble(temp[5]);
                } catch (final Exception e) {
                    throw new AnalyticsException("Data Missing!");
                }
                if (date.getYear() == year && validStatus.contains(temp[4].toLowerCase())) {
                    totalSales += amount;
                    monthWiseSales[date.getMonthValue()][0] += amount;
                }
            }
        } else if (provider.equalsIgnoreCase("amazon") == true) {
            // transaction_id,ext_txn_id,user_id,status,date,amount
            // 0 transaction_id,
            // 1 ext_txn_id,
            // 2 user_id,
            // 3 status,
            // 4 date,
            // 5 amount
            List<String> validStatus = Arrays.asList("shipped");
            //final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while ((line = bReader.readLine()) != null) {
                String temp[] = line.split(DELIMETER);
                try {
                    date = LocalDate.parse(temp[4]);
                    amount = Double.parseDouble(temp[5]);
                } catch (Exception e) {
                    throw new AnalyticsException("Data Missing!");
                }
                if (date.getYear() == year && validStatus.contains(temp[3].toLowerCase())) {
                    totalSales += amount;
                    int m = date.getMonthValue();
                    monthWiseSales[m][0] += amount;
                }
            }
        } else if (provider.equalsIgnoreCase("eBay") == true) {
            // txn_id,username,transaction_status,transaction_date,amount
            // 0 txn_id,
            // 1 username,
            // 2 transaction_status,
            // 3 transaction_date,
            // 4 amount
            List<String> validStatus = Arrays.asList("complete","delivered");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            while ((line = bReader.readLine()) != null) {
                String temp[] = line.split(DELIMETER);
                try {
                    date = LocalDate.parse(temp[3], formatter);
                    amount = Double.parseDouble(temp[4]);
                } catch (Exception e) {
                    throw new AnalyticsException("Data Missing!");
                }
                if (date.getYear() == year && validStatus.contains(temp[2].toLowerCase()) == true) {
                    totalSales += amount;
                    monthWiseSales[date.getMonthValue()][0] += amount;
                }
            }
        }
        aggregateByMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            double sales = monthWiseSales[i][0];
            aggregateByMonths.add(new SaleAggregateByMonth(i, sales));
        }
        bReader.close();
        // System.out.println(totalSales);
        saleAggregate.setTotalSales(totalSales);
        saleAggregate.setAggregateByMonths(aggregateByMonths);
        // printLogs(String.valueOf(totalSales));
        return saleAggregate;
    }
    
}