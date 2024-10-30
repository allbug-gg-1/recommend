package com.sofm.recommend.infrastructure.geo;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Subdivision;
import com.sofm.recommend.common.dto.ProvinceCityDto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
@Slf4j
public class GeoIPService implements InitializingBean, DisposableBean {

    private DatabaseReader dbReader;
    private ByteBuf byteBuf;

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathResource resource = new ClassPathResource("geo/GeoLite2-City.mmdb");
        // 分配堆外内存 ByteBuf，并将文件数据读入其中
        byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer((int) resource.contentLength());
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteBuf.writeBytes(buffer, 0, bytesRead);
            }
        }

        // 使用 ByteBuf 数据构建 DatabaseReader
        byte[] dbData = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, dbData);  // 从 ByteBuf 读取数据
        dbReader = new DatabaseReader.Builder(new ByteArrayInputStream(dbData)).build();
    }

    public ProvinceCityDto getLocationByIP(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);
            Subdivision subdivision = response.getMostSpecificSubdivision();
            City city = response.getCity();
            String provinceName = subdivision.getNames().getOrDefault("zh-CN", null).replace("省", "");
            String cityName = city.getNames().getOrDefault("zh-CN", null).replace("市", "");
            return ProvinceCityDto.of(provinceName, cityName);
        } catch (IOException | GeoIp2Exception e) {
            log.info("search location failed ip:{}", ip);
        }
        return ProvinceCityDto.of();
    }


    @Override
    public void destroy() throws Exception {
        if (byteBuf != null && byteBuf.refCnt() > 0) {
            byteBuf.release();
        }
    }


    public static void main(String[] args) throws Exception {
        GeoIPService geoIPService = new GeoIPService();
        geoIPService.afterPropertiesSet();
        System.out.println(geoIPService.getLocationByIP("112.10.211.33"));
        geoIPService.destroy();
    }


}
