/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemr.calculation.library.hiv.art;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Calculates whether a patient has a transfer out date
 */
public class TransferOutDateCalculation extends AbstractPatientCalculation {

	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection,
	 *      java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues,
										 PatientCalculationContext context) {

		Concept transferOutDate = Dictionary.getConcept(Dictionary.DATE_TRANSFERRED_OUT);
		Concept discontinueQuestion = Dictionary.getConcept(Dictionary.REASON_FOR_PROGRAM_DISCONTINUATION);
		Concept transferOut = Dictionary.getConcept(Dictionary.TRANSFERRED_OUT);
		CalculationResultMap transferInDateResults = Calculations.lastObs(transferOutDate, cohort, context);
		CalculationResultMap discontinueObs = Calculations.lastObs(discontinueQuestion, cohort, context);

		CalculationResultMap result = new CalculationResultMap();
		for(int ptId : cohort){

			Date tDate = EmrCalculationUtils.datetimeObsResultForPatient(transferInDateResults, ptId);
			Obs discoReason = EmrCalculationUtils.obsResultForPatient(discontinueObs, ptId);
			Date dateTo = null;
			if(tDate != null){
				dateTo = tDate;
			}
			if(dateTo == null && discoReason != null && discoReason.getValueCoded().equals(transferOut)) {
				dateTo = discoReason.getObsDatetime();
			}

			result.put(ptId, new SimpleResult(dateTo, this));
		}
		return  result;
	}
}
