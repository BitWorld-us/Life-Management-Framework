ruleset a169x569 {
	meta {
		name "kQuery Twitter Bootstrap Test"
		description <<
			Test Ruleset for Twitter Bootstrap module
		>>

		author "Ed Orcutt, LOBOSLLC"
		logging off

		use module a169x568 alias twitterBootstrap
		use module a169x567 alias siteNav 
	}

	global {

	}

  // ------------------------------------------------------------------------
  rule bootstrap_init {
    select when pageview ".*"
    {
		  twitterBootstrap:init();
		  siteNav:init();
			emit << $K('.popover-test').popover() >>;
    }
  }

  // ------------------------------------------------------------------------
  rule bootstrap_home {
    select when web hash_change newhash "/$"
    {
			siteNav:setActiveNav("#navHome");
			siteNav:showPanel("#sectionHome");
    }
  }

  // ------------------------------------------------------------------------
  rule bootstrap_about {
    select when web hash_change newhash "/about$"
    {
			siteNav:setActiveNav("#navAbout");
			siteNav:showPanel("#sectionAbout");
    }
  }

  // ------------------------------------------------------------------------
  rule bootstrap_contact {
    select when web hash_change newhash "/contact$"
    {
			siteNav:setActiveNav("#navContact");
			siteNav:showPanel("#sectionContact");
    }
  }

  // ------------------------------------------------------------------------
  // Beyond here there be dragons :)
  // ------------------------------------------------------------------------
}
