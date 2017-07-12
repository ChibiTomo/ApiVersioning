package net.chibidevteam.apiversioning.exceptions;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;

public class NoSupportedVersionException extends Exception {

    private static final long serialVersionUID = 671642432478305321L;

    public NoSupportedVersionException() {
        super("There is no supported version for the API. Please check your 'application' file properties or your '"
                + ApiVersioningConfiguration.PROPERTY_FILE + "'");
    }
}
