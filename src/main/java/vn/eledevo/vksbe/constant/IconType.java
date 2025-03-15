package vn.eledevo.vksbe.constant;

import lombok.Getter;

@Getter
public enum IconType {
    FILE("File"),
    FOLDER("Folder"),
    IMAGE("Image"),
    VIDEO("Video"),
    PDF("PDF"),
    AUDIO("Audio"),
    UNKNOWN("Unknown"),
    PERSON("Person"),
    ACCOUNT("Account"),
    CASE("Case"),
    ORGANIZATION("Organization"),
    DEPARTMENT("Department"),
    USB("Usb"),
    MIND_MAP_TEMPLATE("MindMapTemplate"),
    COMPUTER("Computer");
    private final String description;

    IconType(String description) {
        this.description = description;
    }
}
