package com.sofm.recommend.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RankedPNote implements Serializable {

    private int noteId;
    private double qualityScore;

    public RankedPNote(int noteId, double qualityScore) {
        this.noteId = noteId;
        this.qualityScore = qualityScore;
    }


}
