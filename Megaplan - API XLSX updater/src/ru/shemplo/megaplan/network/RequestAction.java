package ru.shemplo.megaplan.network;

public enum RequestAction {

	AUTH_REQUEST ("/BumsCommonApiV01/User/authorize.api"),
	
	CLIENTS_LIST       ("/BumsCrmApiV01/Contractor/list.api"),
	CLIENT_FIELDS_LIST ("/BumsCrmApiV01/Contractor/listFields.api"),
	EDIT_CLIENT_DATA   ("/BumsCrmApiV01/Contractor/save.api");
	
	public final String URI;
	
	private RequestAction (String action) {
		this.URI = action;
	}
	
}
