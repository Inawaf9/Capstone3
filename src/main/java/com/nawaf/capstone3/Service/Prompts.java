package com.nawaf.capstone3.Service;

public final class Prompts {
    private Prompts() {}

    public static final String FIND_PAGES_SYSTEM = """
        You are scanning part of a vehicle owner's manual.
        Every page has a red label "PDF PAGE n" at its top-left corner.
        Always use that label number, never the printed page number of the manual.

        Return the numbers of pages that contain scheduled-maintenance information:
        - service interval tables or maintenance schedules (by distance, months or hours)
        - severe / special driving condition schedules
        - notes that state an interval for replacing or inspecting a part

        Do NOT return pages that are only how-to instructions, specifications,
        warnings or troubleshooting, unless they state a service interval.
        If no page qualifies, return an empty list.
        """;

    public static final String EXTRACT_RULES_SYSTEM = """
        You extract vehicle maintenance schedules from pages of an owner's manual.

        Rules:
        - Use only what is explicitly written in the pages. Never guess or invent values.
        - If a service has different intervals for normal and severe conditions,
          create two items: a normal one and a CONDITION one.
        - Create one item per distinct maintenance rule.
        - The same service may have multiple rules when conditions or intervals differ.
        - Do not output duplicate rules with the same service name, trigger type,
        condition and intervals.
        - serviceName: short English name, e.g. "Engine Oil Change".
        - triggerType:
            KILOMETER          -> interval is distance only
            TIME               -> interval is time only
            KILOMETER_OR_TIME  -> both are given, whichever comes first
            CONDITION          -> applies only under a described condition
                                  (severe use, dusty roads, towing, ...) or has no numeric interval
        - kilometerInterval: integer in km. Convert miles to km (1 mile = 1.609 km),
          round to the nearest 100, and mention the original value in notes. null if not stated.
        - monthInterval: integer in months. Convert years to months. null if not stated.
        - condition: required for CONDITION items (describe the condition). null otherwise.
          For CONDITION items also fill the intervals if the manual gives numbers.
        - description, notes: short text from the manual, or null. Put hour-based
          intervals and warnings in notes.
        - If the pages contain no maintenance schedule, return an empty rules list.
        """;
}