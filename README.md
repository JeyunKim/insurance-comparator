# InsuranceComparator

InsuranceComparator is an application that leverages the HealthCare.gov Marketplace API to help users compare health insurance plans, provider networks, and coverage options. This tool simplifies the process of finding the best health insurance plan for your needs.

## Features
- Compare multiple health insurance plans side by side.
- Detailed information on provider networks and coverage options.
- Easy integration with the HealthCare.gov Marketplace API.
- Support for state-specific marketplace redirects
- ZIP code validation with location information

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- HealthCare.gov API key

### Environment Setup
1. Create a `.env` file in the project root directory
2. Copy the contents from `.env.template`
3. Request an API key from https://developer.cms.gov/marketplace-api/key-request.html
4. Replace the placeholder values with your actual API keys:

HEALTHCARE_GOV_API_KEY=your_api_key_here

### Running the Application
1. Ensure your `.env` file is properly configured
2. Build the project: `mvn clean install`
3. Run the application: `mvn spring-boot:run`
4. Access the application at `http://localhost:8080`

## License
This project is licensed under the MIT License.
