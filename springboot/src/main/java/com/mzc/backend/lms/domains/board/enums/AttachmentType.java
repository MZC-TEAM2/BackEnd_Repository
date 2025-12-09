package com.mzc.backend.lms.domains.board.enums;

/**
 * 첨부파일 유형 Enum
 */
public enum AttachmentType {
    IMAGE("이미지", "jpg,jpeg,png,gif,webp"),
    DOCUMENT("문서", "pdf,doc,docx,hwp,txt"),
    ARCHIVE("압축파일", "zip,rar,7z,tar,gz"),
    PRESENTATION("프레젠테이션", "ppt,pptx"),
    SPREADSHEET("스프레드시트", "xls,xlsx,csv"),
    VIDEO("동영상", "mp4,avi,mov,wmv"),
    AUDIO("오디오", "mp3,wav,m4a"),
    OTHER("기타", "");
    
    private final String description;
    private final String allowedExtensions;
    
    AttachmentType(String description, String allowedExtensions) {
        this.description = description;
        this.allowedExtensions = allowedExtensions;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getAllowedExtensions() {
        return allowedExtensions;
    }
    
    /**
     * 파일 확장자로부터 첨부파일 유형 결정
     */
    public static AttachmentType fromExtension(String extension) {
        String ext = extension.toLowerCase();
        
        for (AttachmentType type : values()) {
            if (type.getAllowedExtensions().contains(ext)) {
                return type;
            }
        }
        return OTHER;
    }
}