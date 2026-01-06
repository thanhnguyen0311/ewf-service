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
            Map<String, WayfairCampaignParentSku> campaignParentSkuCache = new HashMap<>();
            Set<String> processedReportKeys = new HashSet<>();


            // Batch collections
            List<WayfairAdsReportDay> reportBatch = new ArrayList<>(1000);
            List<WayfairCampaign> campaignBatch = new ArrayList<>(100);
            List<WayfairParentSku> parentSkuBatch = new ArrayList<>(100);
            List<WayfairCampaignParentSku> campaignParentSkuBatch = new ArrayList<>(100);

            // Batch size constants
            final int REPORT_BATCH_SIZE = 1000;
            final int ENTITY_BATCH_SIZE = 100;
            List<WayfairCampaign> existingCampaigns = wayfairCampaignRepository.findAll();
            for (WayfairCampaign campaign : existingCampaigns) {
                campaignCache.put(campaign.getCampaignId(), campaign);
            }

            List<WayfairParentSku> existingParentSkus = wayfairParentSkuRepository.findAll();
            for (WayfairParentSku parentSku : existingParentSkus) {
                parentSkuCache.put(parentSku.getParentSku(), parentSku);
            }

            List<WayfairCampaignParentSku> existingRelationships = wayfairCampaignParentSkuRepository.findAll();
            for (WayfairCampaignParentSku rel : existingRelationships) {
                String key = rel.getCampaign().getCampaignId() + "_" + rel.getParentSku().getParentSku();
                campaignParentSkuCache.put(key, rel);
            }
            // Pre-load existing reports to check for duplicates
            Set<String> existingReportKeys = new HashSet<>();
            List<Object[]> existingReports = wayfairAdsReportDayRepository.findAllReportKeys();
            for (Object[] key : existingReports) {
                LocalDate date = (LocalDate) key[0];
                String campId = (String) key[1];
                String pSku = (String) key[2];
                Boolean isB2b = (Boolean) key[3];
                existingReportKeys.add(date + "_" + campId + "_" + pSku + "_" + isB2b.toString().toUpperCase());
            }


            try (CSVReader csvReader = readerBuilder.build()) {
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

                    // Create unique key for this report
                    String reportKey = reportDate + "_" + campaignId + "_" + parentSku + "_" + b2b;
                    // Skip if already processed in current batch or exists in database
                    if (processedReportKeys.contains(reportKey) || existingReportKeys.contains(reportKey)) {
                        continue;
                    }
                    processedReportKeys.add(reportKey);


//                    boolean isReportExist = wayfairAdsReportDayRepository.existsByReportDateAndCampaignIdAndParentSku(reportDate, campaignId, parentSku);
//                    if (isReportExist) {
//                        System.out.println("Report already exist for date: " + reportDate + " and sku: " + parentSku);
//                        continue;
//                    }

                    // Process Campaign
                    WayfairCampaign campaign = campaignCache.get(campaignId);
                    if (campaign == null) {
                        campaign = new WayfairCampaign();
                        campaign.setCampaignId(campaignId);
                        campaign.setCampaignName(campaignName);
                        campaign.setDailyCap(Integer.valueOf(dailyCap));
                        if (!startDate.isEmpty()) campaign.setStartDate(reportDate);
                        campaign.setIsActive(isActive);

                        campaignCache.put(campaignId, campaign);
                        campaignBatch.add(campaign);

                        // Save batch if needed
                        if (campaignBatch.size() >= ENTITY_BATCH_SIZE) {
                            wayfairCampaignRepository.saveAll(campaignBatch);
                            campaignBatch.clear();
                        }
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
                        parentSkuBatch.add(parentSkuEntity);

                        // Save batch if needed
                        if (parentSkuBatch.size() >= ENTITY_BATCH_SIZE) {
                            wayfairParentSkuRepository.saveAll(parentSkuBatch);
                            parentSkuBatch.clear();
                        }
                    } else if (isUpdateBid) {
                        parentSkuEntity.setDefaultBid(Float.valueOf(bid));
                        parentSkuBatch.add(parentSkuEntity);

                        // Save batch if needed
                        if (parentSkuBatch.size() >= ENTITY_BATCH_SIZE) {
                            wayfairParentSkuRepository.saveAll(parentSkuBatch);
                            parentSkuBatch.clear();
                        }
                    }

                    // Process Campaign-ParentSKU relationship
                    String relationshipKey = campaignId + "_" + parentSku;
                    if (!campaignParentSkuCache.containsKey(relationshipKey)) {
                        WayfairCampaignParentSku relation = new WayfairCampaignParentSku();
                        relation.setCampaign(campaign);
                        relation.setParentSku(parentSkuEntity);

                        campaignParentSkuCache.put(relationshipKey, relation);
                        campaignParentSkuBatch.add(relation);

                        // Save batch if needed
                        if (campaignParentSkuBatch.size() >= ENTITY_BATCH_SIZE) {
                            wayfairCampaignParentSkuRepository.saveAll(campaignParentSkuBatch);
                            campaignParentSkuBatch.clear();
                        }
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

                // Save any remaining batches
                if (!campaignBatch.isEmpty()) {
                    wayfairCampaignRepository.saveAll(campaignBatch);
                }

                if (!parentSkuBatch.isEmpty()) {
                    wayfairParentSkuRepository.saveAll(parentSkuBatch);
                }

                if (!campaignParentSkuBatch.isEmpty()) {
                    wayfairCampaignParentSkuRepository.saveAll(campaignParentSkuBatch);
                }

                if (!reportBatch.isEmpty()) {
                    wayfairAdsReportDayRepository.saveAll(reportBatch);
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

                    if (!wayfairCampaignParentSkuRepository.existsByCampaignCampaignIdAndParentSkuParentSku(campaignId, parentSku)){
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

                    WayfairCampaignParentSku wayfairCampaignParentSku = wayfairCampaignParentSkuRepository.findByCampaignCampaignIdAndParentSkuParentSku(campaignId,parentSku);
                    if (wayfairCampaignParentSku == null) continue;
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
