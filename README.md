ScertifyEclipseViews
====================

Views for the Scertify Eclipse plugin (Licensed under LGPL v3 terms)


About Scertify Code
---------------------

Software Risk Control and Technical Debt Management are major challenges companies need to face in order to guarantee the reliability of business-critical applications and the agility of development processes. The measurement of code quality (documentation, conformance with programming best practices, code coverage, duplications) enables IT departments to detect, track and reduce the risk of expensive failures. However, the use of code quality tools still raises some questions, especially when considering their implementation:

How to prevent defects at the earliest possible stage, before a code compilation or delivery?
* Is the automation of code corrections and improvements possible?
* How to smartly plan correction sprints that will actually and quickly reduce the Technical Debt of a project?
* How to educate developers for technical debt control, and raise quality and best practices awareness?

Scertify Code, accessible within the development environment as well from Continuous Integration tools, allows developers, QA and application managers to:
* Continuously track code quality, during the development and before a delivery
* Plan and automate refactoring tasks on a set of components or on a complete application
* Refine code analysis by supporting generic and custom frameworks, and facilitate the addition of company-specific control rules
* Assist developers when correcting their code by providing them with detailed and contextualized solutions

About Scertify Code Eclipse Plugin
-------------------------------------

Scertify Code Eclipse provide, into a single Eclipse plugin, a multi-language (Java and JavaScript are currently supported) and extensible code quality analysis tool. Scertify Code Eclipse warn the developer about his coding pitfall and provide some automatic corrections to fix them directly in Eclipse. The core of Scertify Code integrates some specific and advanced rules (e.g., Hibernate) and other rules provided by the state-of-the-art static analysis tools (e.g., PMD, CheckStyle, JSLint etc.).

About Scertify Code Eclipse Plugin Views
--------------------------------------------

Provide some views for the Scertify  Code Eclipse plugin. 
These views are based on CheckStyle Eclispe plugin (http://eclipse-cs.sourceforge.net/)
licensed under LGPL terms (see license in the 'license' directory).
This Eclipse bundle is thus licensed under LGPL terms.

Following modifications has been done from the original Checkstyle UI plugin:
 * Keep only the code related to the Checkstyle stats views 
 * Do not provide the PDF export of violations results (no more itext lib dependency)
 * Display the number of violations since the last audit
 * Renaming of packages and classes in order to be more consistent with the Scertify Code Eclipse Plugin.
 * Minor reworks of the marker stats view
 * API of the informations providers used to get data in the stats views
 * An implementation of the information providers that analyze all Eclipse problems.

