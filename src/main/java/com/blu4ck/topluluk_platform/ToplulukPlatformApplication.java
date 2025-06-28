package com.blu4ck.topluluk_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Davetli Topluluk Platform - Ana Uygulama Sınıfı
 *
 * Kontrollü üyelik sistemi ile çalışan dijital topluluk platformu:
 * - Supabase entegrasyonu ile modern backend
 * - FLOW ve SPARK içerik sistemleri
 * - İşbirliği ve mesajlaşma özellikleri
 * - Destek ve bağış sistemi
 * - JWT tabanlı güvenlik
 */
@SpringBootApplication
public class ToplulukPlatformApplication {

	public static void main(String[] args) {
		System.out.println("🚀 Davetli Topluluk Platform başlatılıyor...");
		SpringApplication.run(ToplulukPlatformApplication.class, args);
		System.out.println("✅ Platform başarıyla çalışıyor!");
	}
}