package net.chibidevteam.apiversioning.util;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.chibidevteam.apiversioning.annotation.ApiVersion;
import net.chibidevteam.apiversioning.util.helper.VersionHelper;

/**
 * This class translate {@link ApiVersion @ApiVersion} annotation to a {@link Set} of real supported versions.
 * 
 * @author Yannis THOMIAS
 */
public class ApiVersionCondition {

    /** Logger that is available to subclasses */
    protected final Log       logger            = LogFactory.getLog(getClass());

    private SortedSet<String> versions          = new TreeSet<>();
    private SortedSet<String> supportedVersions = new TreeSet<>();

    public ApiVersionCondition(String[] versions, String[] supportedVersions) {
        this(new TreeSet<String>(Arrays.asList(versions)), new TreeSet<String>(Arrays.asList(supportedVersions)));
    }

    public ApiVersionCondition(SortedSet<String> versions, SortedSet<String> supportedVersions) {
        SortedSet<String> newVersions = VersionHelper.getRealVersions(versions, supportedVersions);
        this.versions.addAll(newVersions);
        this.supportedVersions.addAll(supportedVersions);
    }

    public boolean doSupportLast() {
        return !versions.isEmpty() && !supportedVersions.isEmpty()
                && VersionHelper.match(versions.last(), supportedVersions.last(), false);
    }

    public ApiVersionCondition restrictWith(ApiVersionCondition other) {
        versions.retainAll(other.versions);
        return new ApiVersionCondition(versions, supportedVersions);
    }

    public Set<String> getVersions() {
        return versions;
    }

    public Set<String> getSupportedVersions() {
        return supportedVersions;
    }

    @Override
    public String toString() {
        return "ApiVersionCondition: { versions: [" + StringUtils.join(versions, ", ") + "], supportedVersions: ["
                + StringUtils.join(supportedVersions, ", ") + "] }";
    }

}
