package org.openmrs.module.kenyaemr.calculation.library.hiv;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.CalculationUtils;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.DurationUnit;

import java.util.*;

/**
 * Created by codehub on 12/4/15.
 */
public class CtxFromAListOfMedicationOrdersCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap medOrdersObss = Calculations.allObs(Dictionary.getConcept(Dictionary.MEDICATION_ORDERS), cohort, context);
        CalculationResultMap medDuration = Calculations.allObs(Dictionary.getConcept(Dictionary.MEDICATION_DURATION), cohort, context);
        CalculationResultMap medDurationunits = Calculations.allObs(Dictionary.getConcept(Dictionary.DURATION_UNITS), cohort, context);
        Set<Integer> hasTCA = CalculationUtils.patientsThatPass(calculate(new NextOfVisitHigherThanContextCalculation(), cohort, context));
        CalculationResultMap medicationDispensed = Calculations.lastObs(Dictionary.getConcept(Dictionary.COTRIMOXAZOLE_DISPENSED), cohort, context);

        Concept ctx = Dictionary.getConcept(Dictionary.SULFAMETHOXAZOLE_TRIMETHOPRIM);
        Concept dapson = Dictionary.getConcept(Dictionary.DAPSONE);

        for (Integer ptId : cohort) {
            boolean onCtxFromAlistOfMedicationOrders = false;
            ListResult listResult = (ListResult) medOrdersObss.get(ptId);
            List<Obs> allObsOrders = CalculationUtils.extractResultValues(listResult);
            Obs medicationDispensedObs = EmrCalculationUtils.obsResultForPatient(medicationDispensed, ptId);
            //find for duration
            ListResult listResult1 = (ListResult) medDuration.get(ptId);
            List<Obs> allObsDuration = CalculationUtils.extractResultValues(listResult1);
            Collections.reverse(allObsDuration);


            //find the units
            ListResult listResult2 = (ListResult) medDurationunits.get(ptId);
            List<Obs> allObsDurationUnits = CalculationUtils.extractResultValues(listResult2);
            Collections.reverse(allObsDurationUnits);

            Collections.reverse(allObsOrders);
            Obs whenCtxTaken = null;
            for(Obs obs: allObsOrders){
                if((obs.getValueCoded().equals(ctx) || obs.getValueCoded().equals(dapson))){
                    whenCtxTaken = obs;
                    if(obs.getObsDatetime().after(DateUtil.adjustDate(DateUtil.getStartOfMonth(context.getNow()), -1, DurationUnit.DAYS))) {
                        onCtxFromAlistOfMedicationOrders = true;
                    }
                    break;
                }
            }
            int duration = 0;
            for(Obs obs:allObsDuration){
                if(whenCtxTaken != null && whenCtxTaken.getObsGroup().equals(obs.getObsGroup())) {
                    duration = obs.getValueNumeric().intValue();
                    break;
                }
            }

            Concept durationUnits = null;
            for(Obs obs: allObsDurationUnits){
                if(whenCtxTaken != null && whenCtxTaken.getObsGroup().equals(obs.getObsGroup())) {
                    durationUnits = obs.getValueCoded();
                    break;
                }
            }
            //check if such patients have a duration set
            if(whenCtxTaken != null && duration > 0 && durationUnits != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(whenCtxTaken.getObsDatetime());
                //adjust this date with the duration specified
                if(duration < 7) {
                    calendar.add(Calendar.MONTH, duration);
                }
                else {
                    calendar.add(Calendar.DATE, duration);
                }

                if(calendar.getTime().after(DateUtil.getStartOfMonth(context.getNow()))) {
                    onCtxFromAlistOfMedicationOrders = true;
                }
            }

            if(whenCtxTaken != null && hasTCA.contains(ptId)){
                onCtxFromAlistOfMedicationOrders = true;
            }

            if(medicationDispensedObs != null && medicationDispensedObs.getValueCoded().equals(Dictionary.getConcept(Dictionary.YES)) && hasTCA.contains(ptId)){
                onCtxFromAlistOfMedicationOrders = true;
            }
            ret.put(ptId, new BooleanResult(onCtxFromAlistOfMedicationOrders, this));
        }

        return ret;
    }

}
