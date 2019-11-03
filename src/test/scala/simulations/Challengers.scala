package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import baseConfig.BaseSimulation
import io.gatling.core.structure.ScenarioBuilder

class Challengers extends BaseSimulation {
	val scn: ScenarioBuilder = scenario("Challengers Flood IO")
    	.during(20 * 60) {
		    group("Main page") {
			    exec(http("/ (Main Page)")
				    .get("/")
				    .check(status.is(200),
					    css("body.errors").notExists,
					    css("#challenger_step_id", "value")
						    .saveAs("stepId"),
					    css("input[name=\"authenticity_token\"]", "value")
						    .saveAs("authToken")))
		    }
			    .group("Step 1: Start") {
				    exec(http("/start (Step 1)")
					    .post("/start")
					    .formParamSeq(Seq(
						    ("utf8", "✓"),
						    ("authenticity_token", "${authToken}"),
						    ("challenger[step_id]", "${stepId}"),
						    ("challenger[step_number]", "1"),
						    ("commit", "Start")
					    ))
					    .header("content-type", "application/x-www-form-urlencoded")
					    .check(status.is(200),
						    css("body.errors").notExists,
						    css("#challenger_step_id", "value")
							    .saveAs("stepId"),
						    css("#challenger_age > option:nth-of-type(n+2)", "value")
							    .findRandom.saveAs("age")))
			    }
			    .group("Step 2: Select Age") {
				    exec(http("/start (Step 2)")
					    .post("/start")
					    .formParamSeq(Seq(
						    ("utf8", "✓"),
						    ("authenticity_token", "${authToken}"),
						    ("challenger[step_id]", "${stepId}"),
						    ("challenger[step_number]", "2"),
						    ("challenger[age]", "${age}"),
						    ("commit", "Next")
					    ))
					    .check(status.is(200),
						    css("body.errors").notExists,
						    css("#challenger_step_id", "value")
							    .saveAs("stepId"),
						    css(".radio label").findAll.saveAs("radios"),
						    css(".radio > input", "value")
							    .findAll.saveAs("inputs")))
					    .exec(session => {
						    val r = session("radios").as[Vector[String]].map(_.toInt)
						    val i = session("inputs").as[Vector[String]]
						    val idx = r.indexOf(r.max)

						    session.set("maxVal", r(idx))
							    .set("maxKey", i(idx))
							    .removeAll("radios", "inputs")
					    })
			    }
			    .group("Step 3: Input Max") {
				    exec(http("/start (Step 3)")
					    .post("/start")
					    .formParamSeq(Seq(
						    ("utf8", "✓"),
						    ("authenticity_token", "${authToken}"),
						    ("challenger[step_id]", "${stepId}"),
						    ("challenger[step_number]", "3"),
						    ("challenger[largest_order]", "${maxVal}"),
						    ("challenger[order_selected]", "${maxKey}"),
						    ("commit", "Next")
					    ))
					    .check(status.is(200),
						    css("body.errors").notExists,
						    css("#challenger_step_id", "value")
							    .saveAs("stepId"),
						    css("input[id^=\"challenger_order\"]", "name")
							    .findAll.saveAs("orderNames"),
						    css("#new_challenger > input:nth-of-type(3)", "value").saveAs("timeStamp")))
					    .exec(session => {
						    val nextFormSeq = Seq(
							    ("utf8", "✓"),
							    ("authenticity_token", session("authToken").as[String]),
							    ("challenger[step_id]", session("stepId").as[String]),
							    ("challenger[step_number]", "4"),
							    ("commit", "Next")
						    )
						    val inputSeq: Seq[(String, Any)] = for (input <- session("orderNames").as[Vector[String]])
							    yield (input, session("timeStamp").as[String])

						    session.set("inputSeq", nextFormSeq ++ inputSeq)
							    .remove("orderNames")
					    })
			    }
			    .group("Step 4: Click Next") {
				    exec(http("/start (Step 4)")
					    .post("/start")
					    .formParamSeq(_ ("inputSeq").as[Seq[(String, Any)]])
					    .resources(http("/code (Ajax Call)")
						    .get("/code")
						    .check(jsonPath("$.code").saveAs("ajaxCode")))
					    .check(status.is(200),
						    css("body.errors").notExists,
						    css("#challenger_step_id", "value")
							    .saveAs("stepId")))
			    }
			    .group("Step 5: Use Ajax Code") {
				    exec(http("/start (Step 5)")
					    .post("/start")
					    .formParamSeq(Seq(
						    ("utf8", "✓"),
						    ("authenticity_token", "${authToken}"),
						    ("challenger[step_id]", "${stepId}"),
						    ("challenger[step_number]", "5"),
						    ("challenger[one_time_token]", "${ajaxCode}"),
						    ("commit", "Next")
					    ))
					    .check(status.is(200),
						    css("h2").is("You're Done!")))
			    }
//			    .exec(session => {
//				    println(session)
//				    session
//			    })
	    }

	setUp(
		scn.inject(rampUsers(50) during(10 * 60))
	).protocols(httpConf)
}
