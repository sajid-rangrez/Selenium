package com.journal.scrap.scrapper;

public interface ScrapperConfigKeys {

	public static final String URL = "url";
	public static final String JOURNAL_NAME = "name";
	public static final String ID = "id";
	public static final String CSS = "css";
	public static final String CLASS = "class";
	public static final String XPATH = "xpath";
//	public static final String JOURNAL_CONFIG = "src/main/resources/journalConfig.json";
	public static final String JOURNAL_CONFIG = "src/main/resources/configfiles/saudi_Journal_of_anaesthesiaConfig.json";
	public static final String ENV_PROPERTIES = "src/main/resources/env.properties";
	public static final String CONFIG_DIRECTORY = "scrapper.config.directory";
	public static final String SCRAPING_CONFIF = "scraping_config";
	public static final String APPLY_FILTER = "apply_filter";
	public static final String FILTER_CONFIG = "filter_config";
	public static final String INCREASE_PATTERN_IN_LIST_PAGE = "increase_pattern_in_list_page";
	public static final String STARTING_INDEX_FOR_LIST_PAGE = "starting_index_for_list_page";
	public static final String DELEY = "delay";
	public static final String PRODUCTS = "products";
	public static final String LOGIN = "login";
	public static final String SEARCH_INPUT_SELECTOR = "search_input_selector";
	public static final String RESULT_SELECTOR = "results_selector";
	public static final String COOKIE_SELECTOR = "cookie_selector";
	public static final String LINK_ATTRIBUTE = "href";
	public static final String DOI_SELECTOR = "doi_selector";
	public static final String TITLE_SELECTOR = "title_selector";
	public static final String ABSTRACT_SELECTOR = "abstract_selector";
	public static final String AUTHORS_SELECTOR = "authors_selector";
	public static final String EXTRACT_DOI = "extract_doi_from_text";
	public static final String DOI_REGEX = "DOI:\\s*(\\S+)";
	public static final String LOGIN_CONFIG = "login_config";
	public static final String LOGIN_SELECTOR = "login_form_selector";
	public static final String USERNAME_SELECTOR = "username_selector";
	public static final String PASSWORD_SELECTOR = "password_selector";
	public static final String SUBMIT_BTN_SELECTOR = "submit_button_selector";
	public static final String USER_ID = "username-email";
	public static final String PASSWORD = "password";
	public static final String SPLIT_BY = " ";
}
