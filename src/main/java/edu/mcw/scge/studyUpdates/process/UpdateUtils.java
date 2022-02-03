package edu.mcw.scge.studyUpdates.process;

import edu.mcw.scge.dao.implementation.*;
import edu.mcw.scge.datamodel.*;

import java.util.*;

public class UpdateUtils {
    static final long DAY = 24 * 60 * 60 * 1000;
    StudyDao sdao=new StudyDao();
    ExperimentRecordDao edao= new ExperimentRecordDao();
    GuideDao gdao=new GuideDao();
    ModelDao mdao=new ModelDao();
    DeliveryDao deliveryDao=new DeliveryDao();
    EditorDao editorDao=new EditorDao();
    public void updateOtherExperimentalObjects(StudyTierUpdate update) throws Exception {
        int studyId=update.getStudyId();
        List<ExperimentRecord> records=edao.getExperimentRecordsByStudyId(studyId);
        for(ExperimentRecord r:records){
           for(Guide g: gdao.getGuidesByExpRecId(r.getExperimentRecordId())) {
               updateGuideTier(g.getGuide_id(), update.getTier());
           }
            updateModelTier(r.getModelId(), update.getTier());
            updateDeliverySystemTier(r.getDeliverySystemId(), update.getTier());
            updateEditorTier(r.getEditorId(), update.getTier());

        }
    }
    public void updateGuideTier(long guideId, int updatedTier) throws Exception {
        Guide g=gdao.getGuideById(guideId).get(0);
        if(g.getTier()<updatedTier || (g.getTier()>updatedTier && g.getTier()==2)){
            gdao.updateGuideTier(updatedTier, guideId);
        }
    }
    public void updateModelTier(long modelId, int updatedTier) throws Exception {
        Model m=mdao.getModelById(modelId);
        if(m.getTier()<updatedTier || (m.getTier()>updatedTier && m.getTier()==2)){
            mdao.updateModelTier(updatedTier, modelId);
        }
    }
    public void updateDeliverySystemTier(long dsId, int updatedTier) throws Exception {
        for(Delivery d: deliveryDao.getDeliverySystemsById(dsId)){
            if(d.getTier()<updatedTier || (d.getTier()>updatedTier && d.getTier()==2)){
                deliveryDao.updateDeliveryTier(updatedTier, dsId);
            }
        }
    }
    public void updateEditorTier(long editorId, int updatedTIer) throws Exception {
        List<Editor> editors=editorDao.getEditorById(editorId);
        if(editors.size()>0) {
            Editor e = editorDao.getEditorById(editorId).get(0);
            if (e.getTier() < updatedTIer || (e.getTier() > updatedTIer && e.getTier() == 2)) {
                editorDao.updateEditorTier(updatedTIer, editorId);
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
}
