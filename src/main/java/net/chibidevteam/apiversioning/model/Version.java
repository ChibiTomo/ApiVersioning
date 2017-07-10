package net.chibidevteam.apiversioning.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.chibidevteam.apiversioning.helper.ClassHelper;

/**
 * @author Yannis THOMIAS
 */
public class Version implements Comparable<Version> {

    /**
     * The major number of the version.
     * This is often the 'X' in 'X.Y.Z'
     */
    private Integer      major;

    /**
     * The minor number of the version.
     * This is often the 'Y' in 'X.Y.Z'
     */
    private Integer      minor;

    /**
     * The patch number of the version.
     * This is often the 'Z' in 'X.Y.Z'
     */
    private Integer      patch;

    /**
     * Other informations that can be found in a version, such as '-RC1', '-SNAPSHOT', etc.
     */
    private List<String> others = new ArrayList<>();

    public void setNext(String str) {
        if (StringUtils.isBlank(str)) {
            return;
        }

        // If this is an integer
        if (str.matches("^\\d+$")) {
            if (major == null) {
                major = Integer.parseInt(str);
            } else if (minor == null) {
                minor = Integer.parseInt(str);
            } else if (patch == null) {
                patch = Integer.parseInt(str);
            }
        } else {
            others.add(str);
        }
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public Integer getPatch() {
        return patch;
    }

    public void setPatch(Integer patch) {
        this.patch = patch;
    }

    public List<String> getOthers() {
        return others;
    }

    public void setOthers(List<String> others) {
        this.others = others;
    }

    @Override
    public int hashCode() {
        return ClassHelper.hash(Version.class, this);
    }

    @Override
    public boolean equals(Object obj) {
        return ClassHelper.areEquals(Version.class, this, obj);
    }

    @Override
    public int compareTo(Version v) {
        int myMajor = major == null ? 0 : major;
        int vMajor = v.major == null ? 0 : v.major;
        int compMajor = myMajor - vMajor;
        if (compMajor != 0) {
            return compMajor;
        }
        int myMinor = minor == null ? 0 : minor;
        int vMinor = v.minor == null ? 0 : v.minor;
        int compMinor = myMinor - vMinor;
        if (compMinor != 0) {
            return compMinor;
        }

        int myPatch = patch == null ? 0 : patch;
        int vPatch = v.patch == null ? 0 : v.patch;
        return myPatch - vPatch;
    }

    @Override
    public String toString() {
        return "Version: { major: " + major + ", minor: " + minor + ", patch: " + patch + ", others: "
                + StringUtils.join(others, "") + " }";
    }
}
