package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.configuration.DatasourceConfig;
import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.service.LocalService;
import com.danny.ewf_service.utils.ImageCheck;
import com.danny.ewf_service.utils.ImageProcessor;
import com.danny.ewf_service.utils.PythonScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TitleGenerator {

    @Autowired
    private final PythonScript pythonScript;

    @Autowired
    private final ImageCheck imageCheck;

    @Autowired
    private final DatasourceConfig datasourceConfig;

    @Autowired
    private final ImageProcessor imageProcessor;

    @Autowired
    private final LocalService localService;

    public TitleGenerator(PythonScript pythonScript, ImageCheck imageCheck, DatasourceConfig datasourceConfig, ImageProcessor imageProcessor, LocalService localService) {
        this.pythonScript = pythonScript;
        this.imageCheck = imageCheck;
        this.datasourceConfig = datasourceConfig;
        this.imageProcessor = imageProcessor;
        this.localService = localService;
    }

    public void generateLocalTitle() {
        List<LocalProduct> localProductList = localService.getAllLocalProducts();

        boolean isLinkAlive;
        Map<String, String> productMap = new HashMap<>();
        for (LocalProduct localProduct : localProductList){
            if (localProduct.getLocalTitle() != null || !localProduct.getLocalTitle().isEmpty()){
                continue;
            }

            if (localProduct.getProduct().getImages() == null || localProduct.getProduct().getImages().isEmpty()){
                continue;
            }


            ImageProcessor.ImageUrls imageUrls = imageProcessor.parseImageJson(localProduct.getProduct().getImages());
            for (String imgLink : imageUrls.getImg()) {
                isLinkAlive = imageCheck.isImageLinkAlive(imgLink);
                productMap.put("img", imgLink);
                productMap.put("sku", localProduct.getLocalSku());
                if(isLinkAlive) {
                    try {
                        Object result = pythonScript.executePythonScript(
                                "title_ai_generator.py",productMap);
                        break;
                    } catch (Exception e) {
                        throw new RuntimeException("Error generating title", e);
                    }

                }
            }


        }
    }
}
