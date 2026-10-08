package com.nawaf.capstone3.Service;


import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Client.OpenRouterClient;
import com.nawaf.capstone3.Model.*;
import com.nawaf.capstone3.Repository.AiChatHistoryRepository;
import com.nawaf.capstone3.Repository.MaintenanceRecordRepository;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor

public class AiService {

    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final OpenRouterClient client;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

// 1. Ask about vehicle
public String askAboutVehicle(Integer vehicleId, String question) {

    Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

    if (vehicle == null) {
        throw new ApiException("vehicle not found");
    }

    // Get maintenance information directly from the vehicle
    List<MaintenanceRule> maintenanceRules = maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle);

    StringBuilder maintenanceInformation = new StringBuilder();

    for (MaintenanceRule rule : maintenanceRules) {

        maintenanceInformation.append("""
                Service name: %s
                Description: %s
                Category: %s
                Action: %s
                Kilometer interval: %s
                Month interval: %s
                Condition: %s
                Specification: %s
                Capacity: %s
                Notes: %s
                Source: %s

                """.formatted(
                rule.getServiceName(),
                rule.getDescription(),
                rule.getCategory(),
                rule.getAction(),
                rule.getKilometers(),
                rule.getMonthInterval(),
                rule.getCondition(),
                rule.getSpecification(),
                rule.getCapacity(),
                rule.getNotes(),
                rule.getSource()
        ));
    }

    String prompt = """
            You are an intelligent vehicle maintenance assistant.

            Your task is to answer the user's question about their vehicle
            using the vehicle information and maintenance information
            provided below.

            ==============================
            VEHICLE INFORMATION
            ==============================
            Make: %s
            Model: %s
            Year: %s
            Engine: %s
            Fuel Type: %s
            Current Kilometers: %s

            ==============================
            VEHICLE MAINTENANCE INFORMATION
            ==============================
            %s

            ==============================
            USER QUESTION
            ==============================
            %s

            ==============================
            INSTRUCTIONS
            ==============================

            1. Answer the user's question directly and clearly.

            2. Use the vehicle information to make the answer relevant
               to this specific vehicle.

            3. For maintenance-related questions, use the provided
               vehicle maintenance information as the primary source.

            4. If the maintenance information provides a specific
               maintenance interval, requirement, specification,
               capacity, condition, or recommendation, use that
               information in your answer.

            5. Do not invent maintenance intervals, specifications,
               services, or vehicle information.

            6. Do not assume missing information. If the available data
               is not enough to give a reliable answer, clearly explain
               what information is missing.

            7. If the question is about when maintenance is due,
               compare the current kilometers with the maintenance
               intervals provided when possible.

            8. If the question is about a possible vehicle problem,
               provide possible causes and recommended checks based on
               the available vehicle and maintenance information.
               Do not claim a definite diagnosis without enough information.

            9. If the question is unrelated to the vehicle or vehicle
               maintenance, politely explain that you can only assist
               with vehicle-related questions.

            10. Give a clear, practical, and easy-to-understand answer.

            11. Respond in the same language as the user's question.
                If the user asks in Arabic, answer in Arabic.
                If the user asks in English, answer in English.

            Provide the best answer based on the available vehicle
            and maintenance information.
            """.formatted(
            vehicle.getMake(),
            vehicle.getModel(),
            vehicle.getYear(),
            vehicle.getEngine(),
            vehicle.getFuelType(),
            vehicle.getCurrentKilometers(),
            maintenanceInformation.toString(),
            question
    );

    String aiResponse = client.sendPrompt(prompt);

    AiChatHistory history = new AiChatHistory();
    history.setUserMessage(question);
    history.setAiResponse(aiResponse);
    history.setVehicle(vehicle);

    aiChatHistoryRepository.save(history);

    return aiResponse;
}

    // Analyze vehicle problem

    public String analyzeProblem(Integer vehicleId, String problem) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("vehicle not found");
        }

        // Get maintenance information from the vehicle
        List<MaintenanceRule> maintenanceRules =
                maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle);

        StringBuilder maintenanceInformation = new StringBuilder();

        for (MaintenanceRule rule : maintenanceRules) {

            maintenanceInformation.append("""
                Service name: %s
                Description: %s
                Category: %s
                Action: %s
                Kilometer interval: %s
                Month interval: %s
                Condition: %s
                Specification: %s
                Capacity: %s
                Notes: %s
                Source: %s

                """.formatted(
                    rule.getServiceName(),
                    rule.getDescription(),
                    rule.getCategory(),
                    rule.getAction(),
                    rule.getKilometers(),
                    rule.getMonthInterval(),
                    rule.getCondition(),
                    rule.getSpecification(),
                    rule.getCapacity(),
                    rule.getNotes(),
                    rule.getSource()
            ));
        }

        String prompt = """
            You are an intelligent vehicle diagnostic assistant.

            Your task is to analyze the problem described by the user
            using the vehicle information and maintenance information
            provided below.

            ==============================
            VEHICLE INFORMATION
            ==============================
            Make: %s
            Model: %s
            Year: %s
            Engine: %s
            Fuel Type: %s
            Current Kilometers: %s

            ==============================
            VEHICLE MAINTENANCE INFORMATION
            ==============================
            %s

            ==============================
            USER'S PROBLEM
            ==============================
            %s

            ==============================
            INSTRUCTIONS
            ==============================

            1. Analyze the problem based on the vehicle information
               and the maintenance information provided.

            2. Provide the most likely possible causes of the problem.
               Do not claim that any cause is a definite diagnosis.

            3. Provide recommended checks that can help identify
               the cause of the problem.

            4. Provide possible solutions or recommended actions.

            5. If the maintenance information contains a relevant
               maintenance service, inspection, condition, or
               recommendation related to the problem, mention it.

            6. Do not invent maintenance information, specifications,
               or vehicle information that is not provided.

            7. Do not claim a definite diagnosis when the available
               information is not sufficient.

            8. Clearly indicate whether the problem may require urgent
               professional inspection.

            9. If the problem may affect driving safety, clearly warn
               the user and recommend professional inspection when
               appropriate.

            10. If the available information is not enough to analyze
                the problem reliably, clearly explain what additional
                information is needed.

            11. Give a clear, practical, and easy-to-understand answer.

            12. Organize the answer using these sections:

                Possible causes
                Recommended checks
                Possible solution
                Urgency

            """.formatted(
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getEngine(),
                vehicle.getFuelType(),
                vehicle.getCurrentKilometers(),
                maintenanceInformation.toString(),
                problem
        );

        return client.sendPrompt(prompt);
    }


    // Maintenance advice

    public String maintenanceProblem(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("vehicle not found");
        }

        // Get maintenance information from the vehicle
        List<MaintenanceRule> maintenanceRules =
                maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle);

        StringBuilder maintenanceInformation = new StringBuilder();

        for (MaintenanceRule rule : maintenanceRules) {

            maintenanceInformation.append("""
                Service name: %s
                Description: %s
                Category: %s
                Action: %s
                Kilometer interval: %s
                Month interval: %s
                Condition: %s
                Specification: %s
                Capacity: %s
                Notes: %s
                Source: %s

                """.formatted(
                    rule.getServiceName(),
                    rule.getDescription(),
                    rule.getCategory(),
                    rule.getAction(),
                    rule.getKilometers(),
                    rule.getMonthInterval(),
                    rule.getCondition(),
                    rule.getSpecification(),
                    rule.getCapacity(),
                    rule.getNotes(),
                    rule.getSource()
            ));
        }

        String prompt = """
            You are an intelligent vehicle maintenance assistant.

            Your task is to provide personalized maintenance advice
            for the vehicle using the vehicle information and the
            maintenance information provided below.

            ==============================
            VEHICLE INFORMATION
            ==============================
            Make: %s
            Model: %s
            Year: %s
            Engine: %s
            Fuel Type: %s
            Current Kilometers: %s

            ==============================
            VEHICLE MAINTENANCE INFORMATION
            ==============================
            %s

            ==============================
            INSTRUCTIONS
            ==============================

            1. Identify the maintenance services that may be due
               based on the vehicle's current kilometers and the
               maintenance intervals provided.

            2. Identify upcoming maintenance services when possible.

            3. List the items that should be checked, serviced,
               or replaced according to the maintenance information.

            4. Consider both kilometer-based and time-based
               maintenance rules when the required information
               is available.

            5. Use the vehicle maintenance information as the
               primary source for all maintenance recommendations.

            6. Do not invent maintenance intervals, services,
               specifications, or requirements.

            7. If the available information is not enough to determine
               whether a service is due, clearly explain what
               information is missing.

            8. Highlight important maintenance warnings or conditions
               mentioned in the maintenance information.

            9. Give clear, practical, and easy-to-understand advice.

            10. Organize the answer using these sections:

                Currently Due
                Upcoming Maintenance
                Recommended Checks
                Important Warnings

            Provide the best maintenance advice based on the vehicle
            information and the available maintenance information.
            """.formatted(
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getEngine(),
                vehicle.getFuelType(),
                vehicle.getCurrentKilometers(),
                maintenanceInformation.toString()
        );

        return client.sendPrompt(prompt);
    }

    //  Summarize maintenance history
    public String summarizeHistory(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);
        if (vehicle == null) {
            throw new ApiException("vehicle not found");
        }

        // Get maintenance history from Repository
        List<MaintenanceRecord> records = maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle);

        StringBuilder maintenanceHistory = new StringBuilder();

        for (MaintenanceRecord record : records) {
            maintenanceHistory.append("""
                    Service: %s
                    Service Date: %s
                    Kilometers: %s
                    Cost: %s
                    Workshop: %s
                    Notes: %s

                    """.formatted(
                    record.getMaintenanceRule().getServiceName(),
                    record.getServiceDate(),
                    record.getKilometers(),
                    record.getCost(),
                    record.getWorkshop(),
                    record.getNote()
            ));
        }

        String prompt = """
                You are an intelligent vehicle maintenance assistant.

                Summarize the maintenance history of the vehicle
                using the maintenance records provided below.

                ==============================
                VEHICLE INFORMATION
                ==============================
                Make: %s
                Model: %s
                Year: %s
                Engine: %s
                Fuel Type: %s
                Current Kilometers: %s

                ==============================
                MAINTENANCE HISTORY
                ==============================
                %s

                ==============================
                INSTRUCTIONS
                ==============================

                1. Summarize the vehicle's maintenance history clearly.

                2. Identify the main services that were performed.

                3. Mention important maintenance patterns when available.

                4. Mention total or notable maintenance costs when
                   the provided records contain enough information.

                5. Identify frequently performed services when possible.

                6. Do not invent maintenance records or information.

                7. Do not assume information that is not included
                   in the maintenance history.

                8. If there are no maintenance records, clearly state
                   that no maintenance history is available.

                9. Keep the summary practical and easy to understand.

                Organize the answer using:

                - Maintenance Summary
                - Major Services
                - Cost Overview
                - Important Observations
                """.formatted(
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getEngine(),
                vehicle.getFuelType(),
                vehicle.getCurrentKilometers(),
                maintenanceHistory
        );

        return client.sendPrompt(prompt);
    }






}
