package edu.mcw.scge.studyUpdates;

import edu.mcw.scge.dao.implementation.StudyDao;
import edu.mcw.scge.dao.implementation.TierUpdateDao;
import edu.mcw.scge.datamodel.StudyAssociation;
import edu.mcw.scge.datamodel.StudyTierUpdate;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.util.*;


public class Manager {
    private String version;
    static final long DAY = 24 * 60 * 60 * 1000;
    TierUpdateDao tierUpdateDao=new TierUpdateDao();
    StudyDao sdao=new StudyDao();
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
        Map<Integer, List<StudyTierUpdate>> sortedUpdatesMap=getSortedUpdatesMapByStudyId(updates);

       for(Map.Entry e:sortedUpdatesMap.entrySet()){
           boolean disabled=false;
           int stuydId= (int) e.getKey();
           List<StudyTierUpdate> updateList=(List<StudyTierUpdate>)e.getValue();
           for(StudyTierUpdate u:updateList) {
               if (!inLastDay(u)) {
                   if(!disabled) {
                       sdao.disableStudyAssociations(u.getStudyId());
                       disabled=true;
                   }
                   loadStudyUpdates(u);
               }
           }
           if(disabled){
               sdao.updateStudyTier(updateList.get(0));
               tierUpdateDao.delete(stuydId);
           }
       }

    }
    public Map<Integer, List<StudyTierUpdate>> getSortedUpdatesMapByStudyId(List<StudyTierUpdate> updates){
        Map<Integer, List<StudyTierUpdate>> updatesMap=new HashMap<>();
        for(StudyTierUpdate u:updates){
     /*      System.out.println("STUDY_ID:"+ u.getStudyId()+"\t"+inLastDay(u) + "\t"+ u.getModifiedTime()+
                   "*********************************************************");*/
            List<StudyTierUpdate> updateList=new ArrayList<>();
            if(updatesMap.get(u.getStudyId())!=null){
                updateList.addAll(updatesMap.get(u.getStudyId()));
            }
            updateList.add(u);
            updatesMap.put(u.getStudyId(),updateList );
        }
        return updatesMap;
    }
    public void loadStudyUpdates(StudyTierUpdate u) throws Exception {
       if(existsStudy(u)){
           if(u.getTier()==2) {
               StudyAssociation a = new StudyAssociation();
               a.setStudyId(u.getStudyId());
               a.setGroupId(u.getAssociatedGroupId());
               a.setTier(u.getTier());
               a.setModifiedBy(String.valueOf(u.getModifiedBy()));
               if (existsAssociation(u)) {
                   a.setAssociationId(sdao.getAssociations(u).get(0));
                   sdao.updateStudyAssociation(a);
               } else {
                   a.setAssociationId(sdao.getNextKey("study_associations_seq"));
                   sdao.insertStudyAssociations(a);

               }
           }


       }
    }
    public boolean existsStudy(StudyTierUpdate u) throws Exception {
       return sdao.getStudyById(u.getStudyId()).size()>0;
    }
    public boolean existsAssociation(StudyTierUpdate u) throws Exception {
       return sdao.existsAssociation(u);
    }
    public boolean inLastDay(StudyTierUpdate u) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(u.getModifiedDate());
        calendar.add(Calendar.HOUR_OF_DAY, u.getModifiedTime().getHours());
        calendar.add(Calendar.MINUTE, u.getModifiedTime().getMinutes());
        calendar.add(Calendar.SECOND, u.getModifiedTime().getSeconds());
        System.out.println("Calendar tme:"+ calendar.getTime().toString());

        System.out.println(calendar.getTimeInMillis() +"\t"+ (System.currentTimeMillis() - DAY )+"\tDAY: "+DAY);
        System.out.println("DIFFERENCE:"+(((System.currentTimeMillis()-calendar.getTimeInMillis())/(1000*60*60*24))));
        return calendar.getTimeInMillis() > System.currentTimeMillis() - DAY;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}
