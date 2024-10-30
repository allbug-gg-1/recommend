package com.sofm.recommend.application.service.ruleHandler;

import java.util.List;

public interface RuleHandler {

    List<Integer> process (List<Integer> rankedIds);

}
