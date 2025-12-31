package com.danny.ewf_service.utils.imports;


import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import com.danny.ewf_service.entity.wayfair.WayfairCampaign;
import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.entity.wayfair.WayfairParentSku;
import com.danny.ewf_service.repository.WayfairAdsReportDayRepository;
import com.danny.ewf_service.repository.WayfairCampaignParentSkuRepository;
import com.danny.ewf_service.repository.WayfairCampaignRepository;
import com.danny.ewf_service.repository.WayfairParentSkuRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class WayfairReportImport {

    @Autowired
    private final WayfairCampaignRepository wayfairCampaignRepository;

    @Autowired
    private final WayfairParentSkuRepository wayfairParentSkuRepository;

    @Autowired
    private final WayfairCampaignParentSkuRepository wayfairCampaignParentSkuRepository;

    @Autowired
    private final WayfairAdsReportDayRepository wayfairAdsReportDayRepository;

    public void importWayfairReportDaily(String filepath, boolean isUpdateBid) {
        try (InputStream file = getClass().getResourceAsStream(filepath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
            CSVParserBuilder parserBuilder = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withEscapeChar('\\')
                    .withStrictQuotes(false)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreQuotations(false)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);

            CSVParser parser = parserBuilder.build();
            // Create reader with the configured parser
            CSVReaderBuilder readerBuilder = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .withSkipLines(1)
                    .withMultilineLimit(-1); // No limit on multiline fields

            // Support multiple date formats
            DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Cache for campaigns and parent SKUs to reduce database lookups
            Map<String, WayfairCampaign> campaignCache = new HashMap<>();
            Map<String, WayfairParentSku> parentSkuCache = new HashMap<>();
            Set<String> processedReportKeys = new HashSet<>();

            List<WayfairAdsReportDay> batchReports = new ArrayList<>(1000);
            List<WayfairCampaign> batchCampaigns = new ArrayList<>();
            List<WayfairParentSku> batchParentSkus = new ArrayList<>();
            List<WayfairCampaignParentSku> batchCampaignParentSkus = new ArrayList<>();


            try (CSVReader csvReader = readerBuilder.build()) {
                String[] columns;
                int count = 0;

                while ((columns = csvReader.readNext()) != null) {
                    count++;

                    String dateStr = getValueByIndex(columns, 0);
                    String campaignId = getValueByIndex(columns, 1);
                    Boolean b2b = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 2));
                    String campaignName = getValueByIndex(columns, 3);
                    Boolean isActive = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 5));
                    String dailyCap = getValueByIndex(columns, 6);
                    String startDate = getValueByIndex(columns, 8);
                    String parentSku = getValueByIndex(columns, 12);
                    String productName = getValueByIndex(columns, 13);
                    String bid = getValueByIndex(columns, 14);
                    String products = getValueByIndex(columns, 16);
                    String className = getValueByIndex(columns, 17);
                    String clicks = getValueByIndex(columns, 19);
                    String impressions = getValueByIndex(columns, 20);
                    String spend = getValueByIndex(columns, 21);
                    String totalSale = getValueByIndex(columns, 25);
                    String orderQty = getValueByIndex(columns, 26);


                    if (dateStr.isEmpty()) continue;
                    LocalDate reportDate;
                    if (dateStr.contains("/")) {
                        reportDate = LocalDate.parse(dateStr, slashFormatter);
                    } else {
                        reportDate = LocalDate.parse(dateStr, hyphenFormatter);
                    }
                    String reportKey = reportDate + "_" + campaignId + "_" + parentSku;
                    // Skip if we've already processed this key in the current batch
                    if (processedReportKeys.contains(reportKey)) {
                        continue;
                    }
                    processedReportKeys.add(reportKey);


                    boolean isReportExist = wayfairAdsReportDayRepository.existsByReportDateAndCampaignIdAndParentSku(reportDate, campaignId, parentSku);
                    if (isReportExist) {
                        System.out.println("Report already exist for date: " + reportDate + " and sku: " + parentSku);
                        continue;
                    }

                    WayfairAdsReportDay wayfairAdsReportDay;
                    WayfairCampaign wayfairCampaign;

                    Optional<WayfairCampaign> optionalWayfairCampaign = wayfairCampaignRepository.findByCampaignId(campaignId);

                    if (optionalWayfairCampaign.isPresent()) {
                        wayfairCampaign = optionalWayfairCampaign.get();
                    } else {
                        wayfairCampaign = new WayfairCampaign();
                        wayfairCampaign.setCampaignId(campaignId);
                        wayfairCampaign.setCampaignName(campaignName);
                        wayfairCampaign.setDailyCap(Integer.valueOf(dailyCap));
                        if (!startDate.isEmpty()) wayfairCampaign.setStartDate(reportDate);
                        wayfairCampaign.setIsActive(isActive);
                        wayfairCampaign.setIsB2b(b2b);
                        wayfairCampaignRepository.save(wayfairCampaign);
                    }

                    WayfairParentSku wayfairParentSku;
                    Optional<WayfairParentSku> optionalWayfairParentSku = wayfairParentSkuRepository.findByParentSku(parentSku);
                    if (optionalWayfairParentSku.isPresent()) {
                        wayfairParentSku = optionalWayfairParentSku.get();
                        if (isUpdateBid) {
                            wayfairParentSku.setDefaultBid(Float.valueOf(bid));
                        }
                        wayfairParentSkuRepository.save(wayfairParentSku);
                    } else {
                        wayfairParentSku = new WayfairParentSku();
                        wayfairParentSku.setParentSku(parentSku);
                        wayfairParentSku.setProductName(productName);
                        wayfairParentSku.setClassName(className);
                        wayfairParentSku.setDefaultBid(Float.valueOf(bid));
                        wayfairParentSku.setProducts(products);
                        wayfairParentSkuRepository.save(wayfairParentSku);
                        WayfairCampaignParentSku wayfairCampaignParentSku = new WayfairCampaignParentSku();
                        wayfairCampaignParentSku.setCampaign(wayfairCampaign);
                        wayfairCampaignParentSku.setParentSku(wayfairParentSku);
                        wayfairCampaignParentSkuRepository.save(wayfairCampaignParentSku);
                    }
                    Optional<WayfairAdsReportDay> optionalWayfairAdsReportDay = wayfairAdsReportDayRepository.findByReportDateAndCampaignIdAndParentSku(reportDate, campaignId, parentSku);
                    if (optionalWayfairAdsReportDay.isPresent()) {
                        wayfairAdsReportDay = optionalWayfairAdsReportDay.get();
                        wayfairAdsReportDay.setClicks(Integer.valueOf(clicks));
                        wayfairAdsReportDay.setImpressions(Integer.valueOf(impressions));
                        wayfairAdsReportDay.setSpend(Double.valueOf(spend));
                        wayfairAdsReportDay.setTotalSale(Double.valueOf(totalSale));
                        wayfairAdsReportDay.setOrderQuantity(Long.valueOf(orderQty));
                        wayfairAdsReportDay.setBid(Double.valueOf(bid));
                    } else {
                        wayfairAdsReportDay = new WayfairAdsReportDay();
                        wayfairAdsReportDay.setReportDate(reportDate);
                        wayfairAdsReportDay.setClicks(Integer.valueOf(clicks));
                        wayfairAdsReportDay.setImpressions(Integer.valueOf(impressions));
                        wayfairAdsReportDay.setSpend(Double.valueOf(spend));
                        wayfairAdsReportDay.setTotalSale(Double.valueOf(totalSale));
                        wayfairAdsReportDay.setOrderQuantity(Long.valueOf(orderQty));
                        wayfairAdsReportDay.setCampaignId(campaignId);
                        wayfairAdsReportDay.setParentSku(parentSku);
                        wayfairAdsReportDay.setBid(Double.valueOf(bid));
                    }
                    wayfairAdsReportDayRepository.save(wayfairAdsReportDay);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }


    public void importWayfairReportKeywordDaily() {
        try (InputStream file = getClass().getResourceAsStream("/data/keyword_report_day_10.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
            CSVParserBuilder parserBuilder = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withEscapeChar('\\')
                    .withStrictQuotes(false)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreQuotations(false)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);

            CSVParser parser = parserBuilder.build();
            // Create reader with the configured parser
            CSVReaderBuilder readerBuilder = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .withSkipLines(1)
                    .withMultilineLimit(-1); // No limit on multiline fields

            // Support multiple date formats
            DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


            try (CSVReader csvReader = readerBuilder.build()) {
                String[] columns;
                while ((columns = csvReader.readNext()) != null) {

                    String dateStr = getValueByIndex(columns, 0);
                    String campaignId = getValueByIndex(columns, 1);
                    Boolean b2b = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 2));
                    String campaignName = getValueByIndex(columns, 3);
                    Boolean isActive = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 5));
                    String dailyCap = getValueByIndex(columns, 6);
                    String startDate = getValueByIndex(columns, 8);
                    String parentSku = getValueByIndex(columns, 12);
                    String productName = getValueByIndex(columns, 13);
                    String defaultBid = getValueByIndex(columns, 14);
                    String products = getValueByIndex(columns, 16);
                    String className = getValueByIndex(columns, 17);
                    String clicks = getValueByIndex(columns, 19);
                    String impressions = getValueByIndex(columns, 20);
                    String spend = getValueByIndex(columns, 21);
                    String totalSale = getValueByIndex(columns, 25);
                    String orderQty = getValueByIndex(columns, 26);


                    if (dateStr.isEmpty()) continue;
                    LocalDate reportDate;
                    if (dateStr.contains("/")) {
                        reportDate = LocalDate.parse(dateStr, slashFormatter);
                    } else {
                        reportDate = LocalDate.parse(dateStr, hyphenFormatter);
                    }

                    boolean isReportExist = wayfairAdsReportDayRepository.existsByReportDateAndCampaignIdAndParentSku(reportDate, campaignId, parentSku);
                    if (isReportExist) {
                        System.out.println("Report already exist for date: " + reportDate + " and sku: " + parentSku);
                        continue;
                    }

                    WayfairCampaign wayfairCampaign;
                    Optional<WayfairCampaign> optionalWayfairCampaign = wayfairCampaignRepository.findByCampaignId(campaignId);
                    if (optionalWayfairCampaign.isPresent()) {
                        wayfairCampaign = optionalWayfairCampaign.get();
                    } else {
                        wayfairCampaign = new WayfairCampaign();
                        wayfairCampaign.setCampaignId(campaignId);
                        wayfairCampaign.setCampaignName(campaignName);
                        wayfairCampaign.setDailyCap(Integer.valueOf(dailyCap));
                        if (!startDate.isEmpty()) wayfairCampaign.setStartDate(reportDate);
                        wayfairCampaign.setIsActive(isActive);
                        wayfairCampaign.setIsB2b(b2b);
                        wayfairCampaignRepository.save(wayfairCampaign);
                    }
                    WayfairParentSku wayfairParentSku;
                    Optional<WayfairParentSku> optionalWayfairParentSku = wayfairParentSkuRepository.findByParentSku(parentSku);
                    if (optionalWayfairParentSku.isPresent()) {
                        wayfairParentSku = optionalWayfairParentSku.get();
                        wayfairParentSku.setDefaultBid(Float.valueOf(defaultBid));
                        wayfairParentSkuRepository.save(wayfairParentSku);
                    } else {
                        wayfairParentSku = new WayfairParentSku();
                        wayfairParentSku.setParentSku(parentSku);
                        wayfairParentSku.setProductName(productName);
                        wayfairParentSku.setClassName(className);
                        wayfairParentSku.setDefaultBid(Float.valueOf(defaultBid));
                        wayfairParentSku.setProducts(products);
                        wayfairParentSkuRepository.save(wayfairParentSku);
                        WayfairCampaignParentSku wayfairCampaignParentSku = new WayfairCampaignParentSku();
                        wayfairCampaignParentSku.setCampaign(wayfairCampaign);
                        wayfairCampaignParentSku.setParentSku(wayfairParentSku);
                        wayfairCampaignParentSkuRepository.save(wayfairCampaignParentSku);
                    }

                    WayfairAdsReportDay wayfairAdsReportDay = new WayfairAdsReportDay();
                    wayfairAdsReportDay.setReportDate(reportDate);
                    wayfairAdsReportDay.setClicks(Integer.valueOf(clicks));
                    wayfairAdsReportDay.setImpressions(Integer.valueOf(impressions));
                    wayfairAdsReportDay.setSpend(Double.valueOf(spend));
                    wayfairAdsReportDay.setTotalSale(Double.valueOf(totalSale));
                    wayfairAdsReportDay.setOrderQuantity(Long.valueOf(orderQty));
                    wayfairAdsReportDay.setCampaignId(campaignId);
                    wayfairAdsReportDay.setParentSku(parentSku);
                    wayfairAdsReportDayRepository.save(wayfairAdsReportDay);
                    System.out.println("Inserted new report for date: " + reportDate + " and sku: " + parentSku);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    private String getValueByIndex(String[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return "";
        }
        return array[index] != null ? array[index].trim() : "";
    }
}
