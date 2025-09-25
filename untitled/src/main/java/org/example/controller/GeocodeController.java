package org.example.controller;

import org.example.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GeocodeController {

    @Autowired
    private LocationService locationService;

    /**
     * 地理定位测试页面
     */
    @GetMapping("/location-test")
    public String locationTestPage() {
        return "location-test";
    }
    
    /**
     * 逆地理编码 - 根据经纬度获取地址信息
     */
    @GetMapping("/api/geocode/reverse")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用LocationService获取详细的位置信息
            LocationService.LocationInfo locationInfo = locationService.getLocationByCoordinates(lat, lng);
            String fullAddress = locationInfo.getFullAddress();

            result.put("success", true);
            result.put("address", fullAddress);
            result.put("latitude", lat);
            result.put("longitude", lng);

            // 添加详细的地址组件
            Map<String, String> addressComponents = new HashMap<>();
            addressComponents.put("country", locationInfo.country);
            addressComponents.put("province", locationInfo.province);
            addressComponents.put("city", locationInfo.city);
            addressComponents.put("district", locationInfo.district);
            addressComponents.put("street", locationInfo.street);
            result.put("addressComponents", addressComponents);

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "地址解析失败: " + e.getMessage());
            result.put("address", String.format("纬度: %.6f, 经度: %.6f", lat, lng));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    
    /**
     * 正地理编码 - 根据地址获取经纬度（可选功能）
     */
    @GetMapping("/api/geocode/forward")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> forwardGeocode(@RequestParam String address) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里可以实现地址转经纬度的功能
            // 由于API限制，暂时返回模拟数据
            result.put("success", true);
            result.put("address", address);
            result.put("latitude", 39.9042);
            result.put("longitude", 116.4074);
            result.put("message", "这是模拟数据，实际项目中需要调用地图API");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "地址解析失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
