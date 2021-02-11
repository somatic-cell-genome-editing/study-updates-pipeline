package edu.mcw.scge.studyUpdates;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;


public class Manager {
    private String version;
    public static void main(String[] args){
    DefaultListableBeanFactory bf= new DefaultListableBeanFactory();
    new XmlBeanDefinitionReader(bf) .loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
    Manager manager= (Manager) bf.getBean("manager");
    System.out.println(manager.getVersion());
}

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
    
}
