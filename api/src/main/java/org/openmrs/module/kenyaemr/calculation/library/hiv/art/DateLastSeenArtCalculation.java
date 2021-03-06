package org.openmrs.module.kenyaemr.calculation.library.hiv.art;

import org.openmrs.Encounter;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.DurationUnit;

import java.util.Collection;
import java.util.Date;
import java.util.Map;


/**
 * Created by codehub on 9/8/15.
 */
public class DateLastSeenArtCalculation extends AbstractPatientCalculation {


    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {

        Integer outcomePeriod = (params != null && params.containsKey("outcomePeriod")) ? (Integer) params.get("outcomePeriod") : null;
        CalculationResultMap initialArtStart = calculate(new InitialArtStartDateCalculation(), cohort, context);

        if(outcomePeriod != null){
            context.setNow(DateUtil.adjustDate(context.getNow(), outcomePeriod, DurationUnit.MONTHS));
        }
        CalculationResultMap lastEncounter = Calculations.lastEncounter(null, cohort, context);

        CalculationResultMap result = new CalculationResultMap();
        for (Integer ptId : cohort) {
            Encounter encounter = EmrCalculationUtils.encounterResultForPatient(lastEncounter, ptId);
            Date artStartDate = EmrCalculationUtils.datetimeResultForPatient(initialArtStart, ptId);
            Date encounterDate = null;
            if(artStartDate != null) {

                if (encounter != null && encounter.getEncounterDatetime().after(artStartDate)) {
                    encounterDate = encounter.getEncounterDatetime();
                }
                else{
                    encounterDate = artStartDate;
                }

            }
            result.put(ptId, new SimpleResult(encounterDate, this));
        }
        return  result;
    }
}
