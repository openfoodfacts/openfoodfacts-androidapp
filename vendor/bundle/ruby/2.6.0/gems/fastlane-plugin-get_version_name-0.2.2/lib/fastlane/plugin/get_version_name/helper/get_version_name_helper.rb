module Fastlane
  module Helper
    class GetVersionNameHelper
      # class methods that you define here become available in your action
      # as `Helper::GetVersionNameHelper.your_method`
      #
      def self.show_message
        UI.message("Hello from the get_version_name plugin helper!")
      end
    end
  end
end
