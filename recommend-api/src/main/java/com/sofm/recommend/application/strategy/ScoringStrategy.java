package com.sofm.recommend.application.strategy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScoringStrategy {

    public static double calculateScore(int videoDuration, int imageNum, int contentLength) {
        double text_score = Math.min(contentLength * 0.25, 10);
        double media_score = 0d;
        int base_image_score = 35;
        int standard_min_image_count = 8;
        int standard_max_image_count = 12;
        int standard_image_score = 45;
        if (videoDuration == 0 && imageNum > 0) {
            if (imageNum < standard_min_image_count) {
                media_score = base_image_score + standard_image_score - Math.abs(standard_min_image_count - imageNum) * 6;
            } else if (imageNum > standard_max_image_count) {
                media_score = base_image_score + standard_image_score - Math.abs(standard_min_image_count - imageNum) * 4;
            } else {
                media_score = base_image_score + standard_image_score;
            }
        }

        int base_video_score = 45;
        int standard_min_video_duration = 15;
        int standard_max_video_duration = 35;
        int standard_video_score = 45;
        if (videoDuration > 0) {
            if (videoDuration < standard_min_video_duration) {
                media_score = base_video_score + standard_video_score - (Math.abs((standard_min_video_duration - videoDuration) / 5)) * 20;
            } else if (videoDuration > standard_max_video_duration) {
                media_score = base_video_score + standard_video_score - (Math.abs((standard_max_video_duration - videoDuration) / 20)) * 5;
            } else {
                media_score = base_video_score + standard_video_score;
            }
        }
        return Math.max(media_score, 0) + text_score;
    }
}
