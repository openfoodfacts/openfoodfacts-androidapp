module Fastlane
  module Actions
    class GetVersionNameAction < Action
        def self.run(params)
          version_name = "0"
          constant_name ||= params[:ext_constant_name]
          gradle_file_path ||= params[:gradle_file_path]
          if gradle_file_path != nil
              UI.message("The increment_version_code plugin will use gradle file at (#{gradle_file_path})!")
              version_name = getVersionName(gradle_file_path, constant_name)
          else
              app_folder_name ||= params[:app_folder_name]
              UI.message("The get_version_code plugin is looking inside your project folder (#{app_folder_name})!")

              #temp_file = Tempfile.new('fastlaneIncrementVersionCode')
              #foundVersionCode = "false"
              Dir.glob("**/#{app_folder_name}/build.gradle") do |path|
                  UI.message(" -> Found a build.gradle file at path: (#{path})!")
                  version_name = getVersionName(path, constant_name)
              end
          end

          if version_name == "0"
              UI.user_error!("Impossible to find the version name with the specified properties 😭")
          else
              # Store the version name in the shared hash
              Actions.lane_context["VERSION_NAME"]=version_name
              UI.success("👍 Version name found: #{version_name}")
          end

          return version_name

        end


        def self.getVersionName(path, constant_name)
          version_name = "0"
          if !File.file?(path)
              UI.message(" -> No file exist at path: (#{path})!")
              return version_name
          end
          begin
              file = File.new(path, "r")
              while (line = file.gets)
                  if line.include? constant_name
                     versionComponents = line.strip.split(' ')
                     version_name = versionComponents[versionComponents.length - 1].tr("\"","")
                     break
                  end
              end
              file.close
          rescue => err
              UI.error("An exception occured while readinf gradle file: #{err}")
              err
          end
          return version_name
      end



        def self.description
          "Get the version name of an Android project. This action will return the version name of your project according to the one set in your build.gradle file"
        end

        def self.authors
          ["Jems"]
        end

        def self.available_options
          [
            FastlaneCore::ConfigItem.new(key: :app_folder_name,
                                    env_name: "GETVERSIONNAME_APP_VERSION_NAME",
                                 description: "The name of the application source folder in the Android project (default: app)",
                                    optional: true,
                                        type: String,
                               default_value:"app"),
            FastlaneCore::ConfigItem.new(key: :gradle_file_path,
                                    env_name: "GETVERSIONNAME_GRADLE_FILE_PATH",
                                 description: "The relative path to the gradle file containing the version name parameter (default:app/build.gradle)",
                                    optional: true,
                                        type: String,
                               default_value: nil),
             FastlaneCore::ConfigItem.new(key: :ext_constant_name,
                                     env_name: "GETVERSIONNAME_EXT_CONSTANT_NAME",
                                  description: "If the version name is set in an ext constant, specify the constant name (optional)",
                                     optional: true,
                                         type: String,
                                default_value: "versionName")
          ]
        end

        def self.output
          [
            ['VERSION_NAME', 'The version name']
          ]
        end

        def self.is_supported?(platform)
          [:android].include?(platform)
        end
      end
    end
  end
