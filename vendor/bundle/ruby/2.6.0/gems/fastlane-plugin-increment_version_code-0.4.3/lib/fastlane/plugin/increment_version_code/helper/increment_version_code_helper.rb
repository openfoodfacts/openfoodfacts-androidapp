module Fastlane
  module Helper
    class IncrementVersionCodeHelper
      # class methods that you define here become available in your action
      # as `Helper::IncrementVersionCodeHelper.your_method`
      #
      def self.show_message
        UI.message("Hello from the increment_version_code plugin helper!")
      end
    end
  end
end
