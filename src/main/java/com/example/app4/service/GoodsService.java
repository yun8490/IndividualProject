package com.example.app4.service;

import com.example.app4.dto.GoodsDTO;
import com.example.app4.entity.GoodsEntity;
import com.example.app4.entity.GoodsFileEntity;
import com.example.app4.repository.GoodsFileRepository;
import com.example.app4.repository.GoodsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoodsService {
    private final GoodsRepository goodsRepository;
    private final GoodsFileRepository goodsFileRepository;
    public void save(GoodsDTO goodsDTO) throws IOException {
        if (goodsDTO.getGoodsFile().isEmpty()) {
            // 첨부파일이 없을 때
            GoodsEntity goodsEntity = GoodsEntity.toSaveEntity(goodsDTO);
            goodsEntity.setFileAttached(0); // 파일 없음 설정
            goodsRepository.save(goodsEntity);
        } else {
            // 첨부파일이 있을 때
            MultipartFile goodsFile = goodsDTO.getGoodsFile();
            String originalFileName = goodsFile.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
            String savePath = "C:/app_img/" + storedFileName;

            // 이미지 파일을 로컬에 저장
            goodsFile.transferTo(new File(savePath));

            // GoodsEntity 생성 및 파일 정보 설정
            GoodsEntity goodsEntity = GoodsEntity.toSaveFileEntity(goodsDTO);
            goodsEntity.setFileAttached(1); // 파일 있음 설정
            Long savedId = goodsRepository.save(goodsEntity).getId();

            // 저장된 GoodsEntity에 파일 정보 추가
            GoodsEntity savedGoods = goodsRepository.findById(savedId).get();
            GoodsFileEntity goodsFileEntity = GoodsFileEntity.toGoodsFileEntity(savedGoods, originalFileName, storedFileName);
            goodsFileRepository.save(goodsFileEntity);
        }
    }

    @Transactional
    public List<GoodsDTO> findAll() {
        List<GoodsEntity>  goodsEntityList = goodsRepository.findAll();
        List<GoodsDTO> goodsDTOList = new ArrayList<>();

        for(GoodsEntity goodsEntity: goodsEntityList){
            goodsDTOList.add(GoodsDTO.toGoodsDTO(goodsEntity));
        }
        return goodsDTOList;
    }

    public GoodsDTO findById(Long id) {
        Optional<GoodsEntity> optionalGoodsEntity = goodsRepository.findById(id);
        if(optionalGoodsEntity.isPresent()){
            GoodsEntity goodsEntity = optionalGoodsEntity.get();
            GoodsDTO goodsDTO = GoodsDTO.toGoodsDTO(goodsEntity);
            return goodsDTO;
        } else {
            return null;
        }
    }
}
