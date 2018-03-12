package com.tfcamerademo.utils;


public class MapUtils {

		/*
		 * 计算两点之间距离
		 * 
		 * @param start
		 * 
		 * @param end
		 * 
		 * @return 米
		 */
		public static int getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {

			double lon1 = (Math.PI / 180) * lng_a;
			double lon2 = (Math.PI / 180) * lng_b;
			double lat1 = (Math.PI / 180) * lat_a;
			double lat2 = (Math.PI / 180) * lat_b;
			// 地球半径
			double R = 6371;
			// 两点间距离 km,如果想要米的话，结果*1000就可以了
			double d = Math.acos(Math.sin(lat1) * Math.sin(lat2)
					+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))
					* R;
			return Double.valueOf(d * 1000).intValue();
		}
		
		
//		public static double getDistanceInfo(Distribution start, Distribution end) {
//		        double radLat1 = (start.getDimensionality() * Math.PI / 180.0);
//		        double radLat2 = (end.getDimensionality() * Math.PI / 180.0);
//		        double a = radLat1 - radLat2;
//		        double b = (start.getLongitude() - end.getLongitude()) * Math.PI / 180.0;
//		        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
//		                        + Math.cos(radLat1) * Math.cos(radLat2)
//		                        * Math.pow(Math.sin(b / 2), 2)));
//		        s = s * 6371;
//		        s = Math.round(s * 10000) / 10000;
//		        return s;
//		    }

		

  }

