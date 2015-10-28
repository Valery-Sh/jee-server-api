/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Valery
 */
public class ApiDependency {

    private final String groupId;
    private final String artifacId;
    private final String version;
    private final String jarName;

    private Map<String, String> otherTags;

    public static ApiDependency getInstance(String mavenLine) {
        String[] a = mavenLine.split(":");
        String line = a[2].substring(2);
        String[] tags = line.split("/");
        ApiDependency dep;
        if ( tags.length == 4 ) {
            dep = new ApiDependency(tags[0], tags[1], tags[2], null, tags[3]);
        } else {
            dep = new ApiDependency(tags[0], tags[1], tags[2], tags[3],tags[4]);
        }
        return dep;
    }

    public ApiDependency(String groupId, String artifacId,
            String version, String attrs, String jarName) {
        this.groupId = groupId;
        this.artifacId = artifacId;
        this.version = version;
        this.jarName = jarName;
        init(attrs);
    }

    private void init(String attrs) {
        otherTags = new HashMap<>();
        if (attrs == null) {
            return;
        }
        String[] tags = attrs.split(",");
        for (String tag : tags) {
            String[] a = tag.split("=");
            otherTags.put(a[0], a[1]);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifacId() {
        return artifacId;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getOtherTags() {
        return otherTags;
    }

    public String getJarName() {
        return jarName;
    }

    public String[] toStringArray() {
        List<String> tags = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        tags.add("<dependency>");
        sb.append("<groupId>")
                .append(getGroupId())
                .append("</groupId>");
        tags.add(sb.toString());

        sb = new StringBuilder();
        sb.append("<artifactId>")
                .append(getArtifacId())
                .append("</artifactId>");
        tags.add(sb.toString());
        sb = new StringBuilder();
        sb.append("<version>")
                .append(getVersion())
                .append("</version>");
        tags.add(sb.toString());

        otherTags.forEach((k, v) -> {
            StringBuilder sbo = new StringBuilder();
            sbo.append("<")
                    .append(k)
                    .append(">")
                    .append(v)
                    .append("</")
                    .append(k)
                    .append(">");
            tags.add(sbo.toString());
        });
        tags.add("</dependency>");

        String[] a = new String[tags.size()];
        return tags.toArray(a);
    }

}//class
