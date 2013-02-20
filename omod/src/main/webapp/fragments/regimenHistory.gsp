<%
	config.require("history")

	def simpleHistory = kenyaEmrUi.simpleRegimenHistory(config.history, ui)
%>
<table id="regimen-history" class="table-decorated table-vertical">
	<thead>
		<tr>
			<th>Start</th>
			<th>End</th>
			<th>Regimen</th>
			<th>Change Reason</th>
		</tr>
	</thead>
	<tbody>
		<% if (!simpleHistory) { %>
			<tr><td colspan="4">None</td></tr>
		<% } %>
		<% for (def change in simpleHistory) { %>
	  	<tr>
			<td>${ change.startDate }</td>
			<td>${ change.endDate }</td>
			<td style="text-align: left">${ change.regimen.shortDisplay }<br/><small>${ change.regimen.longDisplay }</small></td>
			<td style="text-align: left">
				<% if (change.changeReasons) { %>
				${ change.changeReasons.join(",") }
				<% } %>
			</td>
	  	</tr>
		<% } %>
	</tbody>
</table>