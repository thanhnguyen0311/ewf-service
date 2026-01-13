package com.danny.ewf_service.utils.imports;


import com.danny.ewf_service.entity.wayfair.*;
import com.danny.ewf_service.repository.Wayfair.*;
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
    private final WayfairKeywordRepository wayfairKeywordRepository;

    @Autowired
    private final WayfairAdsReportDayRepository wayfairAdsReportDayRepository;

    @Autowired
    private final WayfairKeywordReportDailyRepository wayfairKeywordReportDailyRepository;

    public void importWayfairReportDaily(String filepath, boolean isUpdateBid) {
        CSVParserBuilder parserBuilder = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .withStrictQuotes(false)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreQuotations(false)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);

        CSVParser parser = parserBuilder.build();

        // Support multiple date formats
        DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate minDate = LocalDate.MAX;
        LocalDate maxDate = LocalDate.MIN;

//        // FIRST PASS - Find min and max dates
//        try (InputStream firstPassStream = getClass().getResourceAsStream(filepath);
//             BufferedReader firstPassReader = new BufferedReader(new InputStreamReader(firstPassStream));
//             CSVReader firstPassCsvReader = new CSVReaderBuilder(firstPassReader)
//                     .withCSVParser(parser)
//                     .withSkipLines(1)
//                     .withMultilineLimit(-1)
//                     .build()) {
//
//            String[] columns;
//            while ((columns = firstPassCsvReader.readNext()) != null) {
//                String dateStr = getValueByIndex(columns, 0);
//                if (dateStr.isEmpty()) continue;
//
//                LocalDate reportDate;
//                if (dateStr.contains("/")) {
//                    reportDate = LocalDate.parse(dateStr, slashFormatter);
//                } else {
//                    reportDate = LocalDate.parse(dateStr, hyphenFormatter);
//                }
//
//                // Update min and max dates
//                if (reportDate.isBefore(minDate)) {
//                    minDate = reportDate;
//                }
//                if (reportDate.isAfter(maxDate)) {
//                    maxDate = reportDate;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error reading CSV file for date range detection", e);
//        }
//

        // SECOND PASS - Process the CSV data
        try (InputStream secondPassStream = getClass().getResourceAsStream(filepath);
             BufferedReader secondPassReader = new BufferedReader(new InputStreamReader(secondPassStream));
             CSVReader csvReader = new CSVReaderBuilder(secondPassReader)
                     .withCSVParser(parser)
                     .withSkipLines(1)
                     .withMultilineLimit(-1)
                     .build()) {


            // Cache for campaigns and parent SKUs to reduce database lookups
            Map<String, WayfairCampaign> campaignCache = new HashMap<>();
            Map<String, WayfairParentSku> parentSkuCache = new HashMap<>();


            // Batch collections
            List<WayfairAdsReportDay> reportBatch = new ArrayList<>(1000);

            // Batch size constants
            final int REPORT_BATCH_SIZE = 1000;
            List<WayfairCampaign> existingCampaigns = wayfairCampaignRepository.findAllByTypeIsContaining("Product");
            System.out.println("Found " + existingCampaigns.size() + " existing campaigns");
            for (WayfairCampaign campaign : existingCampaigns) {
                campaignCache.put(campaign.getCampaignId(), campaign);
            }

            List<WayfairParentSku> existingParentSkus = wayfairParentSkuRepository.findAll();
            System.out.println("Found " + existingParentSkus.size() + " existing parent SKUs");
            for (WayfairParentSku parentSku : existingParentSkus) {
                parentSkuCache.put(parentSku.getParentSku(), parentSku);
            }


            String[] columns;
            int processedRows = 0;


            while ((columns = csvReader.readNext()) != null) {

                String dateStr = getValueByIndex(columns, 0);
                String campaignId = getValueByIndex(columns, 1);
                boolean b2b = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 2));
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

                String compositeKey = reportDate + "_" + campaignId + "_" + parentSku;



                // Process Campaign
                WayfairCampaign campaign = campaignCache.get(campaignId);
                if (campaign == null) {
                    campaign = new WayfairCampaign();
                    campaign.setCampaignId(campaignId);
                    campaign.setCampaignName(campaignName);
                    campaign.setDailyCap(Integer.valueOf(dailyCap));
                    if (!startDate.isEmpty()) campaign.setStartDate(reportDate);
                    campaign.setIsActive(isActive);
                    campaign.setType("Product");

                    campaignCache.put(campaignId, campaign);
                    wayfairCampaignRepository.save(campaign);
                }

                // Process Parent SKU
                WayfairParentSku parentSkuEntity = parentSkuCache.get(parentSku);
                if (parentSkuEntity == null) {
                    parentSkuEntity = new WayfairParentSku();
                    parentSkuEntity.setParentSku(parentSku);
                    parentSkuEntity.setProductName(productName);
                    parentSkuEntity.setClassName(className);
                    parentSkuEntity.setDefaultBid(Float.valueOf(bid));
                    parentSkuEntity.setProducts(products);

                    parentSkuCache.put(parentSku, parentSkuEntity);
                    wayfairParentSkuRepository.save(parentSkuEntity);

                    WayfairCampaignParentSku relation = new WayfairCampaignParentSku();
                    relation.setCampaign(campaign);
                    relation.setParentSku(parentSkuEntity);
                    wayfairCampaignParentSkuRepository.save(relation);
                }


                // Process Report
                WayfairAdsReportDay report = new WayfairAdsReportDay();
                report.setReportDate(reportDate);
                report.setCampaignId(campaignId);
                report.setParentSku(parentSku);
                report.setIsB2b(b2b);
                report.setClicks(Integer.valueOf(clicks));
                report.setImpressions(Integer.valueOf(impressions));
                report.setSpend(Double.valueOf(spend));
                report.setTotalSale(Double.valueOf(totalSale));
                report.setOrderQuantity(Long.valueOf(orderQty));
                report.setBid(Double.valueOf(bid));

                reportBatch.add(report);

                // Save report batch if needed
                if (reportBatch.size() >= REPORT_BATCH_SIZE) {
                    wayfairAdsReportDayRepository.saveAll(reportBatch);
                    reportBatch.clear();
                }

                processedRows++;
                if (processedRows % 1000 == 0) {
                    System.out.println("Processed " + processedRows + " rows");
                }
            }

            if (!reportBatch.isEmpty()) {
                wayfairAdsReportDayRepository.saveAll(reportBatch);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }


    public void importWayfairReportKeywordDaily(String filepath) {
        CSVParserBuilder parserBuilder = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .withStrictQuotes(false)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreQuotations(false)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);

        CSVParser parser = parserBuilder.build();

        // Support multiple date formats
        DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        // First pass: determine min and max dates in the CSV
        LocalDate minDate = LocalDate.MAX; // Initialize to furthest future date
        LocalDate maxDate = LocalDate.MIN; // Initialize to furthest past date

        // FIRST PASS - Find min and max dates
        try (InputStream firstPassStream = getClass().getResourceAsStream(filepath);
             BufferedReader firstPassReader = new BufferedReader(new InputStreamReader(firstPassStream));
             CSVReader firstPassCsvReader = new CSVReaderBuilder(firstPassReader)
                     .withCSVParser(parser)
                     .withSkipLines(1)
                     .withMultilineLimit(-1)
                     .build()) {

            String[] columns;
            while ((columns = firstPassCsvReader.readNext()) != null) {
                String dateStr = getValueByIndex(columns, 0);
                if (dateStr.isEmpty()) continue;

                LocalDate reportDate;
                if (dateStr.contains("/")) {
                    reportDate = LocalDate.parse(dateStr, slashFormatter);
                } else {
                    reportDate = LocalDate.parse(dateStr, hyphenFormatter);
                }

                // Update min and max dates
                if (reportDate.isBefore(minDate)) {
                    minDate = reportDate;
                }
                if (reportDate.isAfter(maxDate)) {
                    maxDate = reportDate;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file for date range detection", e);
        }



        // SECOND PASS - Process the CSV data
        try (InputStream secondPassStream = getClass().getResourceAsStream(filepath);
             BufferedReader secondPassReader = new BufferedReader(new InputStreamReader(secondPassStream));
             CSVReader csvReader = new CSVReaderBuilder(secondPassReader)
                     .withCSVParser(parser)
                     .withSkipLines(1)
                     .withMultilineLimit(-1)
                     .build()) {

            // Cache for campaigns and parent SKUs to reduce database lookups
            Map<String, WayfairCampaign> campaignCache = new HashMap<>();
            Map<Long, WayfairKeyword> keywordCache = new HashMap<>();


            // Batch collections
            List<WayfairKeywordReportDaily> reportBatch = new ArrayList<>(1000);

            // Batch size constants
            final int REPORT_BATCH_SIZE = 1000;
            final int ENTITY_BATCH_SIZE = 100;
            List<WayfairCampaign> existingCampaigns = wayfairCampaignRepository.findAllByTypeIsContaining("Keyword");
            for (WayfairCampaign campaign : existingCampaigns) {
                campaignCache.put(campaign.getCampaignId(), campaign);
            }

            List<WayfairKeyword> existingKeywords = wayfairKeywordRepository.findAll();
            for (WayfairKeyword existingKeyword : existingKeywords) {
                keywordCache.put(existingKeyword.getKeywordId(), existingKeyword);
            }


            // Main processing loop
            String[] columns;
            int processedRows = 0;


            while ((columns = csvReader.readNext()) != null) {

                String dateStr = getValueByIndex(columns, 0);
                String campaignId = getValueByIndex(columns, 1);
                String campaignName = getValueByIndex(columns, 2);
                Boolean isActive = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 4));
                String dailyCap = getValueByIndex(columns, 5);
                String startDate = getValueByIndex(columns, 7);
                Long keywordId = Long.valueOf(getValueByIndex(columns, 9));
                String keywordValue = getValueByIndex(columns, 10);
                String matchType = getValueByIndex(columns, 11);
                Double bid = Double.valueOf(getValueByIndex(columns, 12));
                String clicks = getValueByIndex(columns, 14);
                String impressions = getValueByIndex(columns, 15);
                String spend = getValueByIndex(columns, 16);
                String totalSale = getValueByIndex(columns, 21);
                String orderQty = getValueByIndex(columns, 23);
                String searchTerm = getValueByIndex(columns, 27);
                if (searchTerm.length() > 255) {
                    searchTerm = searchTerm.substring(0, 255);
                }

                if (Long.parseLong(clicks) == 0) {
                    totalSale = String.valueOf(0);
                    orderQty = String.valueOf(0);
                }

                if (dateStr.isEmpty()) continue;
                LocalDate reportDate;
                if (dateStr.contains("/")) {
                    reportDate = LocalDate.parse(dateStr, slashFormatter);
                } else {
                    reportDate = LocalDate.parse(dateStr, hyphenFormatter);
                }

                // Create unique key for this report
                String reportKey = reportDate + "_" + campaignId + "_" + keywordId + "_" + searchTerm;



                // Process Campaign
                WayfairCampaign campaign = campaignCache.get(campaignId);
                if (campaign == null) {
                    campaign = new WayfairCampaign();
                    campaign.setCampaignId(campaignId);
                    campaign.setCampaignName(campaignName);
                    campaign.setDailyCap(Integer.valueOf(dailyCap));
                    if (!startDate.isEmpty()) campaign.setStartDate(reportDate);
                    campaign.setIsActive(isActive);
                    campaign.setType("Keyword");

                    campaignCache.put(campaignId, campaign);
                    wayfairCampaignRepository.save(campaign);


                }

                // Process Parent SKU
                WayfairKeyword keywordEntity = keywordCache.get(keywordId);
                if (keywordEntity == null) {
                    keywordEntity = new WayfairKeyword();
                    keywordEntity.setKeywordId(keywordId);
                    keywordEntity.setKeywordValue(keywordValue);
                    keywordEntity.setDefaultBid(bid);
                    keywordEntity.setType(matchType);

                    keywordCache.put(keywordId, keywordEntity);

                    wayfairKeywordRepository.save(keywordEntity);
                }

                // Process Report
                WayfairKeywordReportDaily report = new WayfairKeywordReportDaily();
                report.setReportDate(reportDate);
                report.setCampaignId(campaignId);
                report.setKeywordId(keywordId);
                report.setSearchTerm(searchTerm);
                report.setClicks(Integer.valueOf(clicks));
                report.setImpressions(Integer.valueOf(impressions));
                report.setSpend(Double.valueOf(spend));
                report.setTotalSale(Double.valueOf(totalSale));
                report.setOrderQuantity(Long.valueOf(orderQty));
                report.setBid(bid);

                reportBatch.add(report);

                if (reportBatch.size() >= REPORT_BATCH_SIZE) {
                    wayfairKeywordReportDailyRepository.saveAll(reportBatch);
                    reportBatch.clear();

                }
                processedRows++;
                if (processedRows % 1000 == 0) {
                    System.out.println("Processed " + processedRows + " rows");
                }


            }



            System.out.println("Saving " + reportBatch.size() + " new reports");

            if (!reportBatch.isEmpty()) {
                wayfairKeywordReportDailyRepository.saveAll(reportBatch);
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


    public void importWayfairParentSkuProduct(String filepath) {
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


            try (CSVReader csvReader = readerBuilder.build()) {
                String[] columns;

                while ((columns = csvReader.readNext()) != null) {

                    String dateStr = getValueByIndex(columns, 0);
                    String campaignId = getValueByIndex(columns, 1);
                    Boolean b2b = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 2));
                    String campaignName = getValueByIndex(columns, 3);
                    Boolean isActive = "TRUE".equalsIgnoreCase(getValueByIndex(columns, 5));
                    String dailyCap = getValueByIndex(columns, 6);
                    String parentSku = getValueByIndex(columns, 12);
                    String productName = getValueByIndex(columns, 13);
                    String bid = getValueByIndex(columns, 14);
                    String products = getValueByIndex(columns, 16);
                    String className = getValueByIndex(columns, 17);


                    WayfairCampaign wayfairCampaign;

                    Optional<WayfairCampaign> optionalWayfairCampaign = wayfairCampaignRepository.findByCampaignId(campaignId);

                    wayfairCampaign = optionalWayfairCampaign.orElseGet(WayfairCampaign::new);
                    wayfairCampaign.setCampaignName(campaignName);
                    wayfairCampaign.setDailyCap(Integer.valueOf(dailyCap));
                    wayfairCampaign.setIsB2b(b2b);
                    wayfairCampaign.setIsActive(isActive);
                    wayfairCampaignRepository.save(wayfairCampaign);

                    WayfairParentSku wayfairParentSku;
                    Optional<WayfairParentSku> optionalWayfairParentSku = wayfairParentSkuRepository.findByParentSku(parentSku);
                    wayfairParentSku = optionalWayfairParentSku.orElseGet(WayfairParentSku::new);
                    wayfairParentSku.setDefaultBid(Float.valueOf(bid));
                    wayfairParentSku.setProductName(productName);
                    wayfairParentSku.setClassName(className);
                    wayfairParentSku.setDefaultBid(Float.valueOf(bid));
                    wayfairParentSku.setProducts(products);
                    wayfairParentSkuRepository.save(wayfairParentSku);

                    if (!wayfairCampaignParentSkuRepository.existsByCampaignCampaignIdAndParentSkuParentSku(campaignId, parentSku)) {
                        WayfairCampaignParentSku newRelationship = new WayfairCampaignParentSku();
                        newRelationship.setCampaign(wayfairCampaign);
                        newRelationship.setParentSku(wayfairParentSku);
                        List<WayfairCampaignParentSku> wayfairParentSkuParentSku = wayfairCampaign.getParentSkus();
                        wayfairParentSkuParentSku.add(newRelationship);
                        wayfairCampaignRepository.save(wayfairCampaign);
                        System.out.println("Inserted new relationship for campaign: " + campaignId + " and sku: " + parentSku);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    public void updateCurrentBid(String filepath) {
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

                    WayfairCampaignParentSku wayfairCampaignParentSku = wayfairCampaignParentSkuRepository.findByCampaignCampaignIdAndParentSkuParentSku(campaignId, parentSku);
                    if (wayfairCampaignParentSku == null) continue;
                    if (b2b) continue;
                    wayfairCampaignParentSku.getParentSku().setDefaultBid(Float.valueOf(defaultBid));
                    wayfairCampaignParentSkuRepository.save(wayfairCampaignParentSku);
                    System.out.println("Updated bid for campaign: " + campaignId + " and sku: " + parentSku);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }
}
