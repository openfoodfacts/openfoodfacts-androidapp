module Fastlane
  module Helper
    class GetVersionCodeHelper
      # class methods that you define here become available in your action
      # as `Helper::GetVersionCodeHelper.your_method`
      #
      def self.show_message
        UI.message("Hello from the get_version_code plugin helper!")
      end
    end
  end
end
