class FileCategory {
    constructor(name, isEditable) {
        this.name = name;
        this.isEditable = isEditable;
    }
}

const FileCategoryEnum = {
    "SCRIPT": new FileCategory("SCRIPT", true),
    "DATA": new FileCategory("DATA", true),
    "LIBRARY": new FileCategory("LIBRARY", false),
    "ETC": new FileCategory("ETC", false),
    "PROJECT": new FileCategory("PROJECT", true)
};

class FileType {
    constructor(name, description, extension, fileCategory, libDistributable, resourceDistributable) {
        this.name = name;
        this.description = description;
        this.extension = extension;
        this.fileCategory = fileCategory;
        this.libDistributable = libDistributable;
        this.resourceDistributable = resourceDistributable;
    }

    isEditable() {
        return this.fileCategory.isEditable;
    }
}

module.exports = {
    FileTypeEnum: {
        /** Python/Jython. */
        'PYTHON_SCRIPT': new FileType("PYTHON_SCRIPT", "Jython Script", "py", FileCategoryEnum.SCRIPT, true, false),
        /** Groovy Script. */
        'GROOVY_SCRIPT': new FileType("GROOVY_SCRIPT", "Groovy Script", "groovy", FileCategoryEnum.SCRIPT, true, false),
        /** Groovy Maven project. */
        'GROOVY_MAVEN_PROJECT': new FileType("GROOVY_MAVEN_PROJECT", "Groovy maven project", "pom", FileCategoryEnum.PROJECT, true, false),
        /** Xml. */
        'XML': new FileType("XML", "xml", "xml", FileCategoryEnum.DATA, true, true),
        /** Text. */
        'TXT': new FileType("TXT", "txt", "txt", FileCategoryEnum.DATA, false, true),
        /** CSV. */
        'CSV': new FileType("CSV", "csv", "csv", FileCategoryEnum.DATA, false, true),
        /** JSON. */
        'JSON': new FileType("JSON", "json", "json", FileCategoryEnum.DATA, false, true),
        /** Properties. */
        'PROPERTIES': new FileType("PROPERTIES", "properties", "properties", FileCategoryEnum.DATA, false, true),
        /** Classes. */
        'CLASS': new FileType("CLASS", "Java Class", "class", FileCategoryEnum.LIBRARY, true, false),
        /** Jar. */
        'JAR': new FileType("JAR", "jar", "jar", FileCategoryEnum.LIBRARY, true, false),
        /** Dll for windows. */
        'DLL': new FileType("DLL", "dll", "dll", FileCategoryEnum.LIBRARY, true, false),
        /** SO for linux. */
        'SO': new FileType("SO", "so", "so", FileCategoryEnum.LIBRARY, true, false),
        /** Unknown. */
        'UNKNOWN': new FileType("UNKNOWN", "unknown", "", FileCategoryEnum.ETC, false, true),
        /** Dir. */
        'DIR': new FileType("DIR", "dir", "", FileCategoryEnum.ETC, false, false)
    },

    getFileTypeByExtension: function getFileTypeByExtension(ext) {
        for (const type in this.FileTypeEnum) {
            if (this.FileTypeEnum[type].extension === ext.toLowerCase()) {
                return this.FileTypeEnum[type];
            }
        }
        return this.FileTypeEnum.UNKNOWN;
    }
};


