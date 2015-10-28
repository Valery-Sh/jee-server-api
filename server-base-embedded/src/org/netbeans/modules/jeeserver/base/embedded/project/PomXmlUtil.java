package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.jeeserver.base.embedded.utils.ParseEntityResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author V. Shyshkin
 */
public class PomXmlUtil {

    private static final Logger LOG = Logger.getLogger(PomXmlUtil.class.getName());

    private Path pomXml;
    private Document doc;

    //private Dependencies dependencies = null;
    public PomXmlUtil(Path pomXml) {
        this.pomXml = pomXml;
        init();
    }
    public PomXmlUtil(InputStream pomXmlStream) {
        init(pomXmlStream);
    }
    private void init(InputStream pomXmlStream) {
        doc = parse(pomXmlStream);
    }

    private void init() {
        doc = parse();
    }

    protected Document parse() {

        Document d = null;
        try {
            FileObject pomFo = FileUtil.toFileObject(pomXml.toFile());
            InputSource source = new InputSource(pomFo.getInputStream());
            d = XMLUtil.parse(source, false, false, null, new ParseEntityResolver());

            //doc.normalizeDocument();
        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return d;
    }

    protected Document parse(InputStream is) {

        Document d = null;
        try {
            FileObject pomFo = FileUtil.toFileObject(pomXml.toFile());
            InputSource source = new InputSource(is);
            d = XMLUtil.parse(source, false, false, null, new ParseEntityResolver());

            //doc.normalizeDocument();
        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return d;
    }
    
    protected Document parse1() {
        Document d = null;

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());
            d = builder.parse(pomXml.toFile());
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            System.err.println("Cannot parse the pom.xml file: " + pomXml);
            System.err.println("   --- Exception.message=" + ex.getMessage());
            LOG.log(Level.INFO, "Parse pom.xml: ", ex);

        }

        return d;
    }

    public void save() throws TransformerConfigurationException, TransformerException {

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(System.out);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }

    public void save(Path targetDir, String newFileName) {
        try {
            Path p = Paths.get(targetDir.toString(),newFileName);
            if ( ! Files.exists(p)) {
                Path dir = Files.createDirectories(targetDir);
                p = Files.createFile(p);
            }
            save(p);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        }
    }    

    
    public void save(Path target) {

        FileObject pomFo = FileUtil.toFileObject(target.toFile());
        try (OutputStream os = pomFo.getOutputStream()) {
            XMLUtil.write(doc, os, doc.getXmlEncoding());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public Dependencies getDependencies() {

        NodeList nl = doc.getDocumentElement().getElementsByTagName("dependencies");
        if (nl == null) {
            return null;
        }

        Element dependenciesElement = (Element) nl.item(0);
        Dependencies dependencies = new Dependencies(dependenciesElement, doc);
        return dependencies;
    }

    public static class Dependencies {

        private Document document;

        private Element element;
        private List<Dependency> dependencyList;

        protected Dependencies() {
        }

        public int size() {
            return list().size();
        }

        protected Dependencies(Element dependenciesTag, Document document) {
            this.element = dependenciesTag;
            this.document = document;
        }

        /*        protected void addChild(Dependency dep) {
         dependencyList.add(dep);
         dep.setDependencies(this);
         }
         */
        protected Element getElement() {
            return element;
        }

        protected void setElement(Element element) {
            this.element = element;
        }

        public List<Dependency> list(final Predicate<Dependency> p) {
            final List<Dependency> list = list();
            final List<Dependency> result = list();
            list.forEach(d -> {
                if (p.test(d)) {
                    result.add(d);
                }
            });
            return result;

        }

        public List<Dependency> list() {
            List<Dependency> list = new ArrayList<>();
            NodeList depList = element.getElementsByTagName("dependency");
            if (depList == null || depList.getLength() == 0) {
                return list;
            }
            for (int i = 0; i < depList.getLength(); i++) {
                Element dependencyElement = (Element) depList.item(i);
                Dependency dependency = new Dependency(dependencyElement);
                dependency.setDependencies(this);
                dependency.list();
                list.add(dependency);
            }
            return list;
        }

        public void delete(String groupId, String artifactId, String version) {
            delete(new Dependency(groupId, artifactId, version));
        }

        public void delete(Predicate<Dependency> p) {
            List<Dependency> list = list();
            list.forEach(d -> {
                if (p.test(d)) {
                    d.delete();
                }
            });
        }

        public void add(List<Dependency> list) {
            list.forEach(d -> add(d));
        }

//        protected Element createElement(String tag, Dependency dep) {
        //if ( dep.get)
//        }
        public boolean delete(Dependency dep) {
            boolean b = true;
            List<Dependency> list = list();
            int idx = list.indexOf(dep);
            if (idx >= 0) {
                Dependency d = list.get(idx);
                d.delete();

            }
            return b;
        }

        public Dependency add(Dependency dep) {
            if (dep.dependencies != null) {
                return null;
            }
            Element el = document.createElement("dependency");
            dep.setElement(el);
            List<DependencyChild> list = new ArrayList<>();
            dep.getNotNullTags().forEach(tag -> {
                DependencyChild c = new DependencyChild(document.createElement(tag[0]));
                c.getElement().setTextContent(tag[1]);
                el.appendChild(c.getElement());
                c.getElement().normalize();
                list.add(c);
            });
            dep.setDependencies(this);
            element.appendChild(el);
            return dep;
        }

    }//class

    public static class Dependency {

        public static final String[] TAGS = new String[]{
            "groupId",
            "artifactId",
            "version",
            "scope",
            "type",
            "classifier",
            "optional",
            "systemPath",
            "exclusions"};

        private Element element;
        private String groupId;
        private String artifactId;
        private String version;
        private String scope;
        private String type;
        private String optional;
        private String classifier;
        private String systemPath;
        //private String exclusions;

        private List<DependencyChild> childs;

        private Dependencies dependencies;

        public Dependency(String groupId, String artifactId, String version) {

            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        protected Dependency(Element dependencyElement) {
            this.element = dependencyElement;
        }

        protected void setDependencies(Dependencies dependencies) {
            this.dependencies = dependencies;
        }

        protected List<String[]> getNotNullTags() {
            List<String[]> list = new ArrayList<>();
            if (groupId != null) {
                list.add(new String[]{"groupId", groupId});
            }
            if (artifactId != null) {
                list.add(new String[]{"artifactId", artifactId});
            }
            if (version != null) {
                list.add(new String[]{"version", version});
            }
            if (scope != null) {
                list.add(new String[]{"scope", scope});
            }
            if (type != null) {
                list.add(new String[]{"type", type});
            }
            if (optional != null) {
                list.add(new String[]{"optional", optional});
            }
            if (classifier != null) {
                list.add(new String[]{"classifier", classifier});
            }
            if (systemPath != null) {
                list.add(new String[]{"systemPath", systemPath});
            }
            list.sort((e1, e2) -> {
                List<String> l = Arrays.asList(TAGS);
                Integer i1 = l.indexOf(e1);
                Integer i2 = l.indexOf(e2);
                return i1.compareTo(i2);
            });
            return list;

        }

        protected void addChild(DependencyChild child) {
            childs.add(child);
            child.setDependency(this);
        }

        protected Element getElement() {
            return element;
        }

        protected void setElement(Element element) {
            this.element = element;
        }

        protected void delete() {
            if (dependencies == null) {
                return;
            }
            dependencies.getElement().removeChild(element);
            dependencies = null;
            element = null;
        }

        protected List<DependencyChild> list() {
            List<DependencyChild> result = new ArrayList<>();

            NodeList nodeList = element.getElementsByTagName("*");

            if (nodeList == null || nodeList.getLength() == 0) {
                return result;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element el = (Element) nodeList.item(i);
                DependencyChild c = new DependencyChild(el);
                c.setDependency(this);

                result.add(new DependencyChild(el));
            }
            //childs = result;
            return result;
        }

        protected String getValue(String tag) {
            String value = null;
            List<DependencyChild> list = list();
            for (DependencyChild c : list) {
                if (tag.equals(c.getElement().getTagName())) {
                    value = c.getValue();
                    break;
                }
            }
            return value;
        }

        public String getGroupId() {
            return dependencies == null ? groupId : getValue("groupId");
        }

        public String getArtifactId() {
            return dependencies == null ? artifactId : getValue("artifactId");
        }

        public String getVersion() {
            return dependencies == null ? version : getValue("version");
        }

        public String getScope() {
            return dependencies == null ? scope : getValue("scope");
        }

        public String getType() {
            return dependencies == null ? type : getValue("type");
        }

        public String getOptional() {
            return dependencies == null ? optional : getValue("optional");
        }

        public String getClassifier() {
            return dependencies == null ? classifier : getValue("classifier");
        }

        public String getSystemPath() {
            return dependencies == null ? systemPath : getValue("systemPath");
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setOptional(String optional) {
            this.optional = optional;
        }

        public void setSystemPath(String systemPath) {
            this.systemPath = systemPath;
        }

        public void setChilds(List<DependencyChild> childs) {
            this.childs = childs;
        }

        protected boolean equals(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return true;
            }
            if (s1 != null) {
                return s1.equals(s2);
            }
            return s2.equals(s1);

        }

        @Override
        public boolean equals(Object other) {
            Dependency o = (Dependency) other;
//System.out.println("***** other=" + other);
            if (other == null) {
                return false;
            }
            boolean b = false;
            /*            System.out.println(" OTHER ----------------------------------------");
             System.out.println("***** other.groupId=" + o.getGroupId());
             System.out.println("***** other.artId=" + o.getArtifactId());
             System.out.println("***** other.verId=" + o.getVersion());
             System.out.println(" THIS ----------------------------------------");
             System.out.println("***** this.groupId=" + this.getGroupId());
             System.out.println("***** this.artId=" + this.getArtifactId());
             System.out.println("***** this.version=" + this.getVersion());
             System.out.println("================================================");

             b = equals(getGroupId(), o.getGroupId())
             && equals(getArtifactId(), o.getArtifactId())
             && equals(getVersion(), o.getVersion());
             System.out.println("RESULT = " + b);
             System.out.println("================================================");
             */
            return equals(getGroupId(), o.getGroupId())
                    && equals(getArtifactId(), o.getArtifactId())
                    && equals(getVersion(), o.getVersion());
        }

    }//class Dependency

    public static class DependencyChild {

        private Element element;
        private Dependency dependency;

        public DependencyChild(String tagName) {
        }

        protected DependencyChild(Element element) {
            this.element = element;
//            this.parentTag = parentTag;
        }

        protected void setDependency(Dependency dependency) {
            this.dependency = dependency;
        }

        protected Element getElement() {
            return element;
        }

        protected void setElement(Element element) {
            this.element = element;
        }

        protected String getValue() {
            String s = element.getTextContent();
            return s == null ? null : element.getTextContent().trim();
        }
    }
    public static class PomProperties {

        private Document document;

        private Element element;

        protected PomProperties() {
        }
        public int size() {
            return list().size();
        }
        
        protected PomProperties(Element propertiesTag, Document document) {
            this.element = propertiesTag;
            this.document = document;
        }
        /**
         * key - a name of the property propertyValue - a newValue of the property
         * 
         * @param pair a collection where key is a property name and propertyValue 
         * is a text content of the property
         */
        public void replaceAll(Map<String,String> pair) {
            pair.forEach((k,v) -> {
                delete(k);
            });
            pair.forEach((k,v) -> {
                Property p = new Property(k);
                p.setValue(v);
                add(p);
            });
        }

        protected Element getElement() {
            return element;
        }

        protected void setElement(Element element) {
            this.element = element;
        }
        
        public List<Property> list(final Predicate<Property> p) {
            final List<Property> list = list();
            final List<Property> result = list();
            list.forEach(d -> {
                if ( p.test(d) ) {
                    result.add(d);
                }
            });
            return result;
            
        }
        public List<Property> list() {
            List<Property> list = new ArrayList<>();
            NodeList propList = element.getElementsByTagName("*");
            if (propList == null || propList.getLength() == 0) {
                return list;
            }
            for (int i = 0; i < propList.getLength(); i++) {
                Element propertyElement = (Element) propList.item(i);
                Property property = new Property(propertyElement);
                property.setProperties(this);
                //property.list();
                list.add(property);
            }
            return list;
        }


        public void delete(String propertyName) {
            delete(new Property(propertyName));
        }

        public void delete(Predicate<Property> p) {
            List<Property> list = list();
            list.forEach(d -> {
                if ( p.test(d)) {
                    d.delete();
                }
            });
        }

        public void add(List<Property> list) {
            list.forEach(d -> add(d));
        }

        public boolean delete(Property prop) {
            boolean b = true;
            List<Property> list = list();
            int idx = list.indexOf(prop);
            if (idx >= 0) {
                Property d = list.get(idx);
                d.delete();

            }
            return b;
        }

        public Property add(Property prop) {
            if (prop.properties != null) {
                return null;
            }
            Element el = document.createElement(prop.getPropertyName());
            prop.setElement(el);
            prop.setProperties(this);
            element.appendChild(el);
            el.setTextContent(prop.getValue());
            return prop;
        }

    }//class

    public static class Property {

        private Element element;
        private String propertyName;
        private String propertyValue;
//        private List<PropertyChild> childs;

        private PomProperties properties;

        public Property(String propertyName) {
            this.propertyName = propertyName;
        }

        protected Property(Element propertyElement) {
            this.element = propertyElement;
            this.propertyName = element.getTagName();
            this.propertyValue = element.getTextContent().trim();
        }

        protected void setProperties(PomProperties properties) {
            this.properties = properties;
        }

        public String getPropertyName() {
            return propertyName;
        }
        
        public String getValue() {
            if ( propertyValue == null ) {
                return "";
            } else {
                return propertyValue;
            }
        }
        protected Element getElement() {
            return element;
        }

        protected void setElement(Element element) {
            this.element = element;
        }

        protected void delete() {
            if (properties == null) {
                return;
            }
            properties.getElement().removeChild(element);
            properties = null;
            element = null;
        }
        protected void replace(Property newProperty) {
            if (properties == null) {
                return;
            }
            properties.getElement().removeChild(element);
            properties = null;
            element = null;
            
            properties.add(newProperty);
        }
        protected void setValue(String value) {
            this.propertyValue = value == null ? "" : value.trim();
        }
        @Override
        public boolean equals(Object other) {
            Property o = (Property) other;
            if (other == null) {
                return false;
            }
            boolean b = o.getPropertyName().equals(getPropertyName()); 
            return b;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.element);
            hash = 23 * hash + Objects.hashCode(this.propertyName);
            hash = 23 * hash + Objects.hashCode(this.propertyValue);
            hash = 23 * hash + Objects.hashCode(this.properties);
            return hash;
        }


    }//class Dependency

    public static class ParserEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String pubid, String sysid)
                throws SAXException, IOException {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }// ParserEntityResolver

    

}//class
