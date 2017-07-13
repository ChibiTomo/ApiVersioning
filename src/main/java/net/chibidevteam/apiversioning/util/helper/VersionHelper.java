package net.chibidevteam.apiversioning.util.helper;

import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.INTEGER_COMPARATOR;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.pojo.Version;

@Service
public final class VersionHelper {

    private static final Log LOGGER = LogFactory.getLog(VersionHelper.class);

    private VersionHelper() {
    }

    /**
     * Test if a version match an array of version representations. The version {@code toTest} should match at least one
     * version representation from the {@code versions}.
     * <ul>
     * <li>1.7 match {^1.5, >2}</li>
     * <li>1.7 match {<1.5, >1.7}</li>
     * <li>1.7 match {!1.5, >1}</li>
     * <li>1.7 does not match {<1.5, ^1}</li>
     * <li>1.7 does not match {^1.5, !1.7}</li>
     * </ul>
     * 
     * @param toTest
     *            the version to test. <b>Should not be a compatibility representation.</b>
     * @param versions
     *            the reference version array.
     * @param isTestFromPath
     *            set it to true if the tested version is from the path.
     * @return {@literal true} if {@code toTest} match at least one version representation from {@code versions}.</br>
     *         {@literal false} if {@code toTest} does not match at least one exclude version from {@code versions}.</br>
     *         {@literal false} otherwise.
     * @throws
     *             NullPointerException
     *             if {@code versions} is null
     */
    public static boolean match(String toTest, String[] versions, boolean isTestFromPath) {
        boolean result = false;
        for (String v : versions) {
            boolean matched = match(toTest, v, isTestFromPath);
            if (isExcludeVersion(v) && !matched) {
                return false;
            }
            result |= matched;
        }
        return result;
    }

    /**
     * Test if two version representation match together
     * <ul>
     * <li>1.7 match >1.5</li>
     * <li>1.7 match ^1.5</li>
     * <li>1.7 match !1.5</li>
     * <li>1.7 does not match <1.5</li>
     * <li>1.7 does not match 1.5</li>
     * </ul>
     * 
     * @param toTest
     *            the version to test. <b>Should not be a compatibility representation.</b>
     * @param version
     *            the reference version.
     * @param isTestFromPath
     *            set it to true if the tested version is from the path.
     * @return {@literal true} if {@code toTest} match the version representation from {@code version}.</br>
     *         {@literal false} otherwise.
     */
    public static boolean match(String toTest, String version, boolean isTestFromPath) {
        boolean isWrongPathVersion = isTestFromPath
                && !toTest.matches(ApiVersioningConfiguration.getPathVersionRegex());
        LOGGER.trace("isTestFromPath: " + isTestFromPath);
        LOGGER.trace("isWrongPathVersion: " + isWrongPathVersion);
        LOGGER.trace("isVersion('" + toTest + "'): " + isVersion(toTest));
        LOGGER.trace("isVersion('" + version + "'): " + isVersion(version));
        if (isWrongPathVersion || !isVersion(toTest) || !isVersion(version)) {
            return false;
        }

        // version is "^x.x.x"
        if (isCompatibilityVersion(version)) {
            return isCompatible(toTest, version);
        }

        // version is ">x.x.x"
        if (isSuperiorityVersion(version)) {
            return isSuperior(toTest, version);
        }

        // version is "<x.x.x"
        if (isInferiorityVersion(version)) {
            return isInferior(toTest, version);
        }

        // version is "!x.x.x"
        if (isExcludeVersion(version)) {
            return isNot(toTest, version);
        }

        // version is "vx.x.x" or "Vx.x.x" or "=x.x.x" or "x.x.x"
        if (isExactlyVersion(version)) {
            return isExact(toTest, version);
        }

        // Something went wrong
        return false;
    }

    /**
     * Test if the passed string match the {@link ApiVersioningConfiguration}::getConfVersionRegex
     * or {@link ApiVersioningConfiguration}::getPathVersionRegex
     * 
     * @param toTest
     * @return
     */
    public static boolean isVersion(String toTest) {
        LOGGER.trace("Testing '" + toTest + "' with '" + ApiVersioningConfiguration.getConfVersionRegex() + "' and '"
                + ApiVersioningConfiguration.getPathVersionRegex() + "'");
        return toTest.matches(ApiVersioningConfiguration.getConfVersionRegex())
                || toTest.matches(ApiVersioningConfiguration.getPathVersionRegex());
    }

    /**
     * @param version
     * @return
     */
    public static String simplify(String version) {
        Version v = fromString(version);
        Integer major = v.getMajor() == null ? 0 : v.getMajor();
        Integer minor = v.getMinor() == null ? 0 : v.getMinor();
        Integer patch = v.getPatch() == null ? 0 : v.getPatch();
        List<String> others = v.getOthers();

        StringBuilder sb = new StringBuilder();
        if (!others.isEmpty()) {
            sb.insert(0, StringUtils.join(v.getOthers(), ""));
        }

        if (sb.length() > 0 || patch != 0) {
            sb.insert(0, patch);
            sb.insert(0, '.');
        }
        if (sb.length() > 0 || minor != 0) {
            sb.insert(0, minor);
            sb.insert(0, '.');
        }
        sb.insert(0, major);
        return sb.toString();
    }

    private static boolean isCompatible(String toTest, String version) {
        Version toTestV = fromString(toTest);
        Version versionV = fromString(version);

        boolean sameMajor = compareMajor(toTestV, versionV) == 0;
        boolean higherMinor = compareMinor(toTestV, versionV) >= 0;
        boolean higherPatch = comparePatch(toTestV, versionV) >= 0;
        return sameMajor && higherMinor && higherPatch;
    }

    private static boolean isSuperior(String toTest, String version) {
        Version toTestV = fromString(toTest);
        Version versionV = fromString(version);

        return toTestV.compareTo(versionV) >= 0;
    }

    private static boolean isInferior(String toTest, String version) {
        Version toTestV = fromString(toTest);
        Version versionV = fromString(version);

        return toTestV.compareTo(versionV) < 0;
    }

    private static boolean isNot(String toTest, String version) {
        return !isExact(toTest, version);
    }

    private static boolean isExact(String toTest, String version) {
        Version toTestV = fromString(toTest);
        Version versionV = fromString(version);
        return toTestV.compareTo(versionV) == 0;
    }

    private static boolean isCompatibilityVersion(String version) {
        return version.matches(ApiVersioningConfiguration.getCompatibleVersionRegex());
    }

    private static boolean isSuperiorityVersion(String version) {
        return version.matches(ApiVersioningConfiguration.getSuperiorVersionRegex());
    }

    private static boolean isInferiorityVersion(String version) {
        return version.matches(ApiVersioningConfiguration.getInferiorVersionRegex());
    }

    private static boolean isExcludeVersion(String version) {
        return version.matches(ApiVersioningConfiguration.getExcludeVersionRegex());
    }

    private static boolean isExactlyVersion(String version) {
        return version.matches(ApiVersioningConfiguration.getExactVersionRegex());
    }

    private static int compareMajor(Version v1, Version v2) {
        return INTEGER_COMPARATOR.compare(v1.getMajor(), v2.getMajor());
    }

    private static int compareMinor(Version v1, Version v2) {
        return INTEGER_COMPARATOR.compare(v1.getMinor(), v2.getMinor());
    }

    private static int comparePatch(Version v1, Version v2) {
        return INTEGER_COMPARATOR.compare(v1.getPatch(), v2.getPatch());
    }

    private static Version fromString(String str) {
        String newStr = str;
        if (newStr.startsWith(ApiVersioningConfiguration.getVersionPathPrefix())) {
            newStr = newStr.replaceFirst(ApiVersioningConfiguration.getVersionPathPrefix(), "");
        }
        Matcher m = ApiVersioningConfiguration.getVersionPattern().matcher(newStr);
        Version result = new Version();
        if (m.find()) {
            int grpCount = m.groupCount();
            for (int i = 1; i < grpCount; ++i) {
                result.setNext(m.group(i));
            }
        }
        return result;
    }
}
