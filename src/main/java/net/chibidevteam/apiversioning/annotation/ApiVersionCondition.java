package net.chibidevteam.apiversioning.annotation;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import net.chibidevteam.apiversioning.helper.VersionHelper;

/**
 * This class translate {@link ApiVersion} annotation to a {@link Set} of real supported versions.
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
        SortedSet<String> newVersions = getRealVersions(versions, supportedVersions);
        this.versions.addAll(newVersions);
        this.supportedVersions.addAll(supportedVersions);
    }

    public boolean doSupportLast() {
        return !versions.isEmpty() && !supportedVersions.isEmpty()
                && VersionHelper.match(versions.last(), supportedVersions.last(), false);
    }

    private SortedSet<String> getRealVersions(SortedSet<String> versions, SortedSet<String> supportedVersions) {
        if (CollectionUtils.isEmpty(supportedVersions)) {
            logger.trace("No supported version");
            return new TreeSet<>();
        }
        if (CollectionUtils.isEmpty(versions)) {
            logger.trace("Support all versions");
            return supportedVersions;
        }
        SortedSet<String> result = new TreeSet<>();
        for (String sv : supportedVersions) {
            for (String v : versions) {
                logger.trace("Testing '" + sv + "' over '" + v + "' => "
                        + (VersionHelper.match(sv, v, false) ? "does match" : "does NOT match"));
                if (VersionHelper.match(sv, v, false)) {
                    result.add(VersionHelper.simplify(sv));
                }
            }
        }
        return result;
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
