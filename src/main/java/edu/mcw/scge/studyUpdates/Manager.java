package edu.mcw.scge.studyUpdates;

import edu.mcw.scge.dao.implementation.*;
import edu.mcw.scge.datamodel.*;
import edu.mcw.scge.studyUpdates.process.UpdateUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.util.*;


public class Manager {
    private String version;
   
    TierUpdateDao tierUpdateDao=new TierUpdateDao();
    StudyDao sdao=new StudyDao();
    UpdateUtils utils=new UpdateUtils();


    public static void main(String[] args){
    DefaultListableBeanFactory bf= new DefaultListableBeanFactory();
    new XmlBeanDefinitionReader(bf) .loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
    Manager manager= (Manager) bf.getBean("manager");
    try{
        manager.run(args);
    }catch (Exception e){
        e.printStackTrace();
    }
    System.out.println(manager.getVersion());
    }

    public void run(String[] args) throws Exception {
        List<StudyTierUpdate> updates= tierUpdateDao.getStudyTierUpdates();
        Map<Integer, List<StudyTierUpdate>> sortedUpdatesMap=utils.getSortedUpdatesMapByStudyId(updates);

       for(Map.Entry e:sortedUpdatesMap.entrySet()){
           boolean disabled=false;
           int stuydId= (int) e.getKey();
           List<StudyTierUpdate> updateList=(List<StudyTierUpdate>)e.getValue();
           for(StudyTierUpdate u:updateList) {
               if (!utils.inLastDay(u)) {
                   if(!disabled) {
                       sdao.disableStudyAssociations(u.getStudyId());
                       disabled=true;
                   }
                   utils.loadStudyUpdates(u);
               }
           }
           if(disabled){
               sdao.updateStudyTier(updateList.get(0));
               utils.updateOtherExperimentalObjects(updateList.get(0));
               tierUpdateDao.delete(stuydId);
           }
       }

    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}
