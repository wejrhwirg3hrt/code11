package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = false)
public class LocationService {
    
    // 中国主要城市的地理位置数据
    private static final List<CityInfo> CITIES = Arrays.asList(
        // 直辖市
        new CityInfo("北京市", "北京市", 39.9042, 116.4074, 
            Arrays.asList("东城区", "西城区", "朝阳区", "丰台区", "石景山区", "海淀区", "门头沟区", "房山区")),
        new CityInfo("上海市", "上海市", 31.2304, 121.4737,
            Arrays.asList("黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区", "杨浦区", "浦东新区")),
        new CityInfo("天津市", "天津市", 39.3434, 117.3616,
            Arrays.asList("和平区", "河东区", "河西区", "南开区", "河北区", "红桥区", "滨海新区")),
        new CityInfo("重庆市", "重庆市", 29.5647, 106.5507,
            Arrays.asList("渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "渝北区")),
            
        // 省会城市
        new CityInfo("广东省", "广州市", 23.1291, 113.2644,
            Arrays.asList("越秀区", "海珠区", "荔湾区", "天河区", "白云区", "黄埔区", "番禺区", "花都区")),
        new CityInfo("广东省", "深圳市", 22.5431, 114.0579,
            Arrays.asList("罗湖区", "福田区", "南山区", "宝安区", "龙岗区", "盐田区", "龙华区", "坪山区")),
        new CityInfo("江苏省", "南京市", 32.0603, 118.7969,
            Arrays.asList("玄武区", "秦淮区", "建邺区", "鼓楼区", "浦口区", "栖霞区", "雨花台区", "江宁区")),
        new CityInfo("浙江省", "杭州市", 30.2741, 120.1551,
            Arrays.asList("上城区", "下城区", "江干区", "拱墅区", "西湖区", "滨江区", "萧山区", "余杭区")),
        new CityInfo("山东省", "济南市", 36.6512, 117.1201,
            Arrays.asList("历下区", "市中区", "槐荫区", "天桥区", "历城区", "长清区", "章丘区")),
        new CityInfo("四川省", "成都市", 30.5728, 104.0668,
            Arrays.asList("锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区", "新都区")),
        new CityInfo("湖北省", "武汉市", 30.5928, 114.3055,
            Arrays.asList("江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "青山区", "洪山区", "东西湖区")),
        new CityInfo("河南省", "郑州市", 34.7466, 113.6254,
            Arrays.asList("中原区", "二七区", "管城区", "金水区", "上街区", "惠济区", "郑东新区")),
        new CityInfo("陕西省", "西安市", 34.3416, 108.9398,
            Arrays.asList("新城区", "碑林区", "莲湖区", "灞桥区", "未央区", "雁塔区", "阎良区", "临潼区")),
        new CityInfo("辽宁省", "沈阳市", 41.8057, 123.4315,
            Arrays.asList("和平区", "沈河区", "大东区", "皇姑区", "铁西区", "苏家屯区", "浑南区", "沈北新区")),
        new CityInfo("黑龙江省", "哈尔滨市", 45.8038, 126.5349,
            Arrays.asList("道里区", "南岗区", "道外区", "平房区", "松北区", "香坊区", "呼兰区", "阿城区")),
            
        // 其他重要城市
        new CityInfo("山东省", "青岛市", 36.0671, 120.3826,
            Arrays.asList("市南区", "市北区", "黄岛区", "崂山区", "李沧区", "城阳区", "即墨区")),
        new CityInfo("福建省", "厦门市", 24.4798, 118.0894,
            Arrays.asList("思明区", "海沧区", "湖里区", "集美区", "同安区", "翔安区")),
        new CityInfo("江苏省", "苏州市", 31.2989, 120.5853,
            Arrays.asList("虎丘区", "吴中区", "相城区", "姑苏区", "工业园区", "高新区")),
        new CityInfo("浙江省", "宁波市", 29.8683, 121.5440,
            Arrays.asList("海曙区", "江北区", "北仑区", "镇海区", "鄞州区", "奉化区")),
        new CityInfo("广东省", "东莞市", 23.0489, 113.7447,
            Arrays.asList("莞城街道", "南城街道", "东城街道", "万江街道", "石碣镇", "石龙镇"))
    );
    
    /**
     * 根据经纬度获取最近的城市信息
     */
    public LocationInfo getLocationByCoordinates(double lat, double lng) {
        CityInfo nearestCity = findNearestCity(lat, lng);
        
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.country = "中国";
        locationInfo.province = nearestCity.province;
        locationInfo.city = nearestCity.name;
        
        // 根据距离城市中心的位置选择区县
        locationInfo.district = selectDistrict(nearestCity, lat, lng);
        
        // 生成街道信息
        locationInfo.street = generateStreetInfo(nearestCity, lat, lng);
        
        return locationInfo;
    }
    
    /**
     * 查找最近的城市
     */
    private CityInfo findNearestCity(double lat, double lng) {
        CityInfo nearestCity = CITIES.get(0);
        double minDistance = calculateDistance(lat, lng, nearestCity.latitude, nearestCity.longitude);
        
        for (CityInfo city : CITIES) {
            double distance = calculateDistance(lat, lng, city.latitude, city.longitude);
            if (distance < minDistance) {
                minDistance = distance;
                nearestCity = city;
            }
        }
        
        return nearestCity;
    }
    
    /**
     * 选择区县
     */
    private String selectDistrict(CityInfo city, double lat, double lng) {
        if (city.districts.isEmpty()) {
            return "市区";
        }
        
        // 简单的区县选择逻辑（基于相对位置）
        double cityLat = city.latitude;
        double cityLng = city.longitude;
        
        if (lat > cityLat && lng > cityLng) {
            return city.districts.get(0); // 东北方向
        } else if (lat > cityLat && lng < cityLng) {
            return city.districts.get(Math.min(1, city.districts.size() - 1)); // 西北方向
        } else if (lat < cityLat && lng > cityLng) {
            return city.districts.get(Math.min(2, city.districts.size() - 1)); // 东南方向
        } else {
            return city.districts.get(Math.min(3, city.districts.size() - 1)); // 西南方向
        }
    }
    
    /**
     * 生成街道信息
     */
    private String generateStreetInfo(CityInfo city, double lat, double lng) {
        String[] streetSuffixes = {"大道", "路", "街", "巷", "胡同", "广场"};
        String[] streetPrefixes = {"中山", "人民", "解放", "建设", "和平", "友谊", "文化", "科技", "商业", "工业"};
        
        // 基于城市和坐标生成街道名
        int prefixIndex = (int) ((lat + lng) * 1000) % streetPrefixes.length;
        int suffixIndex = (int) (lat * lng * 1000) % streetSuffixes.length;
        
        return streetPrefixes[prefixIndex] + streetSuffixes[suffixIndex];
    }
    
    /**
     * 计算两点间距离（简化版）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2));
    }
    
    /**
     * 城市信息类
     */
    public static class CityInfo {
        public String province;
        public String name;
        public double latitude;
        public double longitude;
        public List<String> districts;
        
        public CityInfo(String province, String name, double latitude, double longitude, List<String> districts) {
            this.province = province;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.districts = districts;
        }
    }
    
    /**
     * 位置信息类
     */
    public static class LocationInfo {
        public String country;
        public String province;
        public String city;
        public String district;
        public String street;
        
        public String getFullAddress() {
            StringBuilder address = new StringBuilder();
            if (country != null) address.append(country).append(" ");
            if (province != null) address.append(province).append(" ");
            if (city != null) address.append(city).append(" ");
            if (district != null) address.append(district).append(" ");
            if (street != null) address.append(street);
            return address.toString().trim();
        }
    }
}
